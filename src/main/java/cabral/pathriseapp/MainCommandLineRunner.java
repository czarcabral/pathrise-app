package cabral.pathriseapp;

import cabral.pathriseapp.model.Job;
import cabral.pathriseapp.model.JobBoard;
import cabral.pathriseapp.service.MainService;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MainCommandLineRunner implements CommandLineRunner {

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int BATCH_SIZE;

    @Autowired
    private MainService mainService;

    private Logger logger = LoggerFactory.getLogger(MainCommandLineRunner.class);

    @Override
    public void run(String... args) throws Exception {
        // read and save jobBoards.json and job_opportunities.csv
        initializeJobs();
    }

    // create jobs and jobboards
    private void initializeJobs() {
        // use to compare companyName to jobBoards domain
        Map<String, JobBoard> jobBoardsMap = new HashMap<>();

        // use to keep track of amount of Jobs per Job Source
        Map<String, Integer> jobSourceCountMap = new HashMap<>();

        // extract JobBoards from jobBoards.json
        Resource jobBoardsJson = new ClassPathResource("jobBoards.json");
        ObjectMapper mapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .build();
        Map<String, List<JobBoard>> outerMap = null;
        try {
            logger.info("Begin reading jobBoards.json and creating JobBoards");

            // convert json object to map
            outerMap = mapper.readValue(jobBoardsJson.getInputStream(), new TypeReference<>() {});
            List<JobBoard> jobBoards = outerMap.get("job_boards");

            mainService.saveJobBoards(jobBoards);

            // shortcut : make key value pairs of root domains and job board object
            for (JobBoard jobBoard : jobBoards) {
                jobBoardsMap.put(jobBoard.getRootDomain().toLowerCase(), jobBoard);
            }

            logger.info("Done reading jobBoards.json and creating JobBoards");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // extract Jobs from job_opportunities.csv
        Resource jobOpportunitiesCsv = new ClassPathResource("job_opportunities.csv");
        File jobOpportunitiesResolvedCsv = new File("job_opportunities_resolved.csv");
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(jobOpportunitiesCsv.getInputStream()))
                .withSkipLines(1)
                .build();
             CSVWriter writer = new CSVWriter(new FileWriter(jobOpportunitiesResolvedCsv));
        ) {
            logger.info("Begin reading job_opportunities.csv and creating Jobs");

            // the resolved jobs to be written into the output csv file
            List<String[]> outputData = new ArrayList<>();
            outputData.add(new String[] {"ID (primary key)", "Job Title", "Company Name", "Job URL", "Job Source" });

            List<Job> jobs = new ArrayList<>();

            // read each record and convert to Job
            String[] line = null;
            while ((line = csvReader.readNext()) != null) {   // temp
//            for (int i = 0; i < 100; i++) {                    // temp
//                line = csvReader.readNext();                    // temp

                Job job = new Job();

                // id
                line[0] = line[0].replace("\"", "");
                if (StringUtils.isNumeric(line[0])) {
                    job.setId(Integer.parseInt(line[0]));
                }

                // jobTitle
                line[1] = line[1].replace("\"", "");
                if (!line[1].isEmpty()) {
                    job.setJobTitle(line[1]);
                }

                // companyName
                line[2] = line[2].replace("\"", "");
                if (!line[2].isEmpty()) {
                    job.setCompanyName(line[2]);
                }

                // jobUrl
                line[3] = line[3].replace("\"", "");
                if (!line[3].isEmpty()) {
                    job.setJobUrl(line[3]);
                }

                // jobSource
                job.setJobSource(determineJobSource(job.getJobUrl(), job.getCompanyName(), jobBoardsMap, mapper));

                jobs.add(job);

                if (!jobs.isEmpty() && jobs.size() % BATCH_SIZE == 0) {
                    mainService.saveJobs(jobs);
                    jobs.clear();
                }

                outputData.add(new String[] {line[0], line[1], line[2], line[3], job.getJobSource() });

                int currentCount = jobSourceCountMap.getOrDefault(job.getJobSource(), 0);
                jobSourceCountMap.put(job.getJobSource(), currentCount + 1);
            }
            if (!jobs.isEmpty()) {
                mainService.saveJobs(jobs);
                jobs.clear();
            }

            // write all the resolved jobs
            writer.writeAll(outputData);

            // create json file to keep track of count of jobs by job source
            mapper.writeValue(new File("job_source_count.json"), jobSourceCountMap);

            logger.info("Done reading job_opportunities.csv and creating Jobs");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // determine job source of job record
    private String determineJobSource(String jobUrl, String companyName, Map<String, JobBoard> jobBoardsMap, ObjectMapper mapper) {
        // we can't do anything if there is no URL
        if (jobUrl != null) {
            try {
                // Grab the host from the URL
                jobUrl = jobUrl.split("[#?]")[0];
                URI uri = new URI(jobUrl);
                String host = uri.getHost();

                // check if valid URI host
                if (host != null) {
                    // extract the domain from the URL and check it is a known job board domain
                    String[] hostArr = host.split("\\.");
                    if (hostArr.length > 1) {
                        String domain = hostArr[hostArr.length - 2] + "." + hostArr[hostArr.length - 1];
                        if (jobBoardsMap.containsKey(domain.toLowerCase())) {
                            JobBoard jobBoard = jobBoardsMap.get(domain.toLowerCase());
                            return jobBoard.getName();
                        }
                    }

                    // check if company on host is same as companyName from csv record
                    // split company names into tokens and if any part of the company name appears in host, return company name
                    // e.g. Veson Nautical https://careers-veson.icims.com
                    String[] companyNameTokens = companyName.split("((?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z]))|[^a-zA-Z]");
                    for (String companyNameToken : companyNameTokens) {
                        if (host.contains(companyNameToken.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""))) {
                            return "Company Website";
                        }
                    }

                    // also try to see if company name can be converted to acronym
                    // UPDATE: this will produce false positives because the acronym may be part of random substring in domain
                }
            } catch (URISyntaxException e) {
//                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "Unknown";
    }
}
