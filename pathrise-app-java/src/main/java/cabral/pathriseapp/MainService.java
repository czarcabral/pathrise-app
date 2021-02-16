package cabral.pathriseapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainService {

    @Autowired
    private JobBoardRepository jobBoardRepository;

    @Autowired
    private JobRepository jobRepository;

    public void saveJobBoards(List<JobBoard> jobBoards) {
        jobBoardRepository.saveAll(jobBoards);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public void saveJob(Job job) {
        jobRepository.save(job);
    }
}
