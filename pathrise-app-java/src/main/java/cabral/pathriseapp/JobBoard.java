package cabral.pathriseapp;

public class JobBoard {

    private String name;
    private String rating;
    private String rootDomain;
    private String logoFile;
    private String description;

    public JobBoard(String name, String rating, String rootDomain, String logoFile, String description) {
        this.name = name;
        this.rating = rating;
        this.rootDomain = rootDomain;
        this.logoFile = logoFile;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRootDomain() {
        return rootDomain;
    }

    public void setRootDomain(String rootDomain) {
        this.rootDomain = rootDomain;
    }

    public String getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(String logoFile) {
        this.logoFile = logoFile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
