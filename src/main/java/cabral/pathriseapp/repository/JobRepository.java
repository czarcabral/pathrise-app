package cabral.pathriseapp.repository;

import cabral.pathriseapp.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {

    @Query(value = "SELECT * FROM job WHERE job_source = :myJobSource", nativeQuery = true)
    public List<Job> myCustomQuery(@Param("myJobSource") String jobSource);
}
