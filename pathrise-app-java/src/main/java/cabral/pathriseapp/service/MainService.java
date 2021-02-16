package cabral.pathriseapp.service;

import cabral.pathriseapp.model.Job;
import cabral.pathriseapp.model.JobBoard;
import cabral.pathriseapp.repository.JobBoardRepository;
import cabral.pathriseapp.repository.JobRepository;
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
