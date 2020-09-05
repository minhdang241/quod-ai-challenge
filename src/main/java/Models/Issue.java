package Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {
    private String openDate;
    private String closeDate;

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(String closeDate) {
        this.closeDate = closeDate;
    }

    //    public LocalDate getOpenDate() {
//        return openDate;
//    }
//
//    public void setOpenDate(String dateInString) {
//        this.openDate = LocalDate.parse(dateInString, DateTimeFormatter.ISO_INSTANT);
//    }
//
//    public LocalDate getCloseDate() {
//        return closeDate;
//    }
//
//    public void setCloseDate(String dateInString) {
//        this.closeDate = LocalDate.parse(dateInString, DateTimeFormatter.ISO_INSTANT);
//    }
}
