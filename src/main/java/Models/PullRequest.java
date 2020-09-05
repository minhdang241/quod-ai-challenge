package Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {
    private String createdDate;
    private String mergedDate;

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getMergedDate() {
        return mergedDate;
    }

    public void setMergedDate(String mergedDate) {
        this.mergedDate = mergedDate;
    }
}
