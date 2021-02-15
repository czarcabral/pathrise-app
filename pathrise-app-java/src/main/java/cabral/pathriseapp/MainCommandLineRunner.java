package cabral.pathriseapp;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class MainCommandLineRunner implements CommandLineRunner {

    @Autowired
    private MainService mainService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("TEMP IS RUNNING");

        initializeJobs();
    }

    private void initializeJobs() {
        // set up job boards map
        Resource jobBoardsJson = new ClassPathResource("jobBoards.json");
        ObjectMapper mapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .build();
        Map<String, Object> jobBoardsMap = null;
        try {
            jobBoardsMap = mapper.readValue(jobBoardsJson.getInputStream(), new TypeReference<>() {});

            // shortcut : make key value pairs of root domains and job board object
            Map<String, String>[] jobBoards = mapper.convertValue(jobBoardsMap.get("job_boards"), new TypeReference<>() {});
            for (Map<String, String> jobBoard : jobBoards) {
                jobBoardsMap.put(jobBoard.get("root_domain").toLowerCase(), jobBoard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create new Job out of each record in job opportunities csv
        Resource jobOpportunitiesCsv = new ClassPathResource("job_opportunities.csv");
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(jobOpportunitiesCsv.getInputStream()))) {
            csvReader.readNext(); // skip over header
            String[] line = null;
            while ((line = csvReader.readNext()) != null) {
//            for (int i = 0; i < 100; i++) {
//                line = csvReader.readNext();

                Integer id = 0;
                String jobTitle = null;
                String companyName = null;
                String jobUrl = null;
                if (line.length >= 1) {
                    line[0] = line[0].replace("\"", "");
                    if (StringUtils.isNumeric(line[0])) {
                        id = Integer.valueOf(line[0]);
                    }
                }
                if (line.length >= 2) {
                    line[1] = line[1].replace("\"", "");
                    jobTitle = (line[1].isEmpty()) ? null : line[1];
                }
                if (line.length >= 3) {
                    line[2] = line[2].replace("\"", "");
                    companyName = (line[2].isEmpty()) ? null : line[2];
                }
                if (line.length >= 4) {
                    line[3] = line[3].replace("\"", "");
                    jobUrl = (line[3].isEmpty()) ? null : line[3];
                }

                String jobSource = determineJobSource(jobUrl, companyName, jobBoardsMap, mapper);

                mainService.saveJob(new Job(id, jobTitle, companyName, jobUrl, jobSource));
            }
            System.out.println("Done adding job records");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String determineJobSource(String jobUrl, String companyName, Map<String, Object> jobBoardsMap, ObjectMapper mapper) {
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
                        Map<String, String> jobBoard = mapper.convertValue(jobBoardsMap.get(domain.toLowerCase()), new TypeReference<>() {});
                        return jobBoard.get("name");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "Unknown";
    }
}
