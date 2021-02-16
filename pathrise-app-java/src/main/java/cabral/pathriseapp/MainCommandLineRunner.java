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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MainCommandLineRunner implements CommandLineRunner {

    @Autowired
    private MainService mainService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("TEMP IS RUNNING");

        // read and save jobBoards.json and job_opportunities.csv
        initializeJobs();
    }

    // create jobs and jobboards
    private void initializeJobs() {
        // use to compare companyName to jobBoards domain
        Map<String, JobBoard> jobBoardsMap = new HashMap<>();

        // extract JobBoards from jobBoards.json
        Resource jobBoardsJson = new ClassPathResource("jobBoards.json");
        ObjectMapper mapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .build();
        Map<String, List<JobBoard>> outerMap = null;
        try {
            // convert json object to map
            outerMap = mapper.readValue(jobBoardsJson.getInputStream(), new TypeReference<>() {});
            List<JobBoard> jobBoards = outerMap.get("job_boards");

            mainService.saveJobBoards(jobBoards);

            // shortcut : make key value pairs of root domains and job board object
            for (JobBoard jobBoard : jobBoards) {
                jobBoardsMap.put(jobBoard.getRootDomain().toLowerCase(), jobBoard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // extract Jobs from job_opportunities.csv
        Resource jobOpportunitiesCsv = new ClassPathResource("job_opportunities.csv");
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(jobOpportunitiesCsv.getInputStream()))
                .withSkipLines(1)
                .build()
        ) {
            // read each record and convert to Job
            String[] line = null;
            while ((line = csvReader.readNext()) != null) {
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

                mainService.saveJob(job);
            }
            System.out.println("Done adding job records");
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

                // check if company on host is same as companyName from csv record
                // split company names into tokens and if any part of the company name appears in host, return company name
                // e.g. Veson Nautical https://careers-veson.icims.com
                String[] companyNameTokens = companyName.split("((?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z]))|[^a-zA-Z]");
                for (String companyNameToken : companyNameTokens) {
                    if (host.contains(companyNameToken.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""))) {
                        return companyName;
                    }
                }

                // also try to see if company name can be converted to acronym
                // UPDATE: this will produce false positives because the acronym may be part of random substring in domain

                // extract the domain from the URL and check it is a known job board domain
                String[] hostArr = host.split("\\.");
                if (hostArr.length > 1) {
                    String domain = hostArr[hostArr.length - 2] + "." + hostArr[hostArr.length - 1];
                    if (jobBoardsMap.containsKey(domain.toLowerCase())) {
                        JobBoard jobBoard = jobBoardsMap.get(domain.toLowerCase());
                        return jobBoard.getName();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "Unknown";
    }
}
