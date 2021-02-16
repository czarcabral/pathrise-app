package cabral.pathriseapp.model;

import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "job")
@DynamicInsert
public class Job {

    @Id
    @Column
    Integer id;

    @Column
    String jobTitle;

    @Column
    String companyName;

    @Column
    String jobUrl;

    @Column
    String jobSource;

    public Job() { }

    public Job(Integer id, String jobTitle, String companyName, String jobUrl, String jobSource) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.jobUrl = jobUrl;
        this.jobSource = jobSource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getJobSource() {
        return jobSource;
    }

    public void setJobSource(String jobSource) {
        this.jobSource = jobSource;
    }
}
