package cabral.pathriseapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private MainService mainService;

    @GetMapping("api/jobs")
    public ResponseEntity<?> getJobs() {
        Map<String, Object> map = new HashMap<>();
        List<Job> jobs = mainService.getAllJobs();
        map.put("data", jobs);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
