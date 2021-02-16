package cabral.pathriseapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobBoardRepository extends JpaRepository<JobBoard, Integer> {
}
