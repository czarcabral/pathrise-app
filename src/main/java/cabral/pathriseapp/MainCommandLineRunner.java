package cabral.pathriseapp;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;

@Component
public class MainCommandLineRunner implements CommandLineRunner {

    @Autowired
    private MainService mainService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("TEMP IS RUNNING");

        readJobOpportunities();
    }

    private void readJobOpportunities() {
        Resource resource = new ClassPathResource("job_opportunities.csv");
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            reader.readNext(); // skip over header
            String[] line = null;
            while ((line = reader.readNext()) != null) {
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

                mainService.saveJob(new Job(id, jobTitle, companyName, jobUrl, null));
            }
            System.out.println("Done adding job records");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
