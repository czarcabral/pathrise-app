package cabral.pathriseapp.controller;

import cabral.pathriseapp.model.Job;
import cabral.pathriseapp.model.JobBoard;
import cabral.pathriseapp.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class MainController {

    @Autowired
    private MainService mainService;

    @GetMapping("/jobBoards")
    public ResponseEntity<?> getAllJobBoards() {
        Map<String, Object> map = new HashMap<>();
        List<JobBoard> jobBoards = mainService.getAllJobBoards();
        map.put("data", jobBoards);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs() {
        Map<String, Object> map = new HashMap<>();
        List<Job> jobs = mainService.getAllJobs();
        map.put("data", jobs);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
