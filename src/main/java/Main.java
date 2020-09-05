import Models.Events.Event;
import Models.RepoStat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Main {

    public static void main(String[] args) {

        /*
        1. Write all the relevant info into the file to reduce the size of file
        2. Read that file into the memory, then use it to calculate the statistic metric for each repo
        3. Write it back to the file after processing
        * */

        int numberOfDay = 1;
        Map<String, List<Event>> eventsByRepo  = new HashMap<>();
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream("2020-09-03-15.json");
            sc = new Scanner(inputStream, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
//                System.out.println(line);
//                System.out.println(System.getProperty("line.separator"));
//                System.out.println(line);


                try {
                    Event event = objectMapper.readValue(line, Event.class);
                    String repoId = event.getRepo().getId();
                    if (eventsByRepo.getOrDefault(repoId, null) == null) {
                        List<Event> events = new ArrayList<>();
                        events.add(event);
                        eventsByRepo.put(repoId, events);
                    } else {
                        List<Event> events = eventsByRepo.get(repoId);
                        events.add(event);
                    }

                } catch (JsonMappingException e) {
                    String line2 = sc.nextLine();
                    Event event = objectMapper.readValue(line + line2, Event.class);
                    String repoId = event.getRepo().getId();
                    if (eventsByRepo.getOrDefault(repoId, null) == null) {
                        List<Event> events = new ArrayList<>();
                        events.add(event);
                        eventsByRepo.put(repoId, events);
                    } else {
                        List<Event> events = eventsByRepo.get(repoId);
                        events.add(event);
                    }
                }





            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        eventsByRepo.forEach((String id, List<Event> events) -> {
            String repoName = "";
            int numberOfCommits = 0;

            int numberOfClosedIssue = 0;
            long totalHourIssueRemainOpen = 0;
            long avgTimeIssueRemainOpen = 0;

            int numberOfMergedPR = 0;
            int totalHourToMergePR = 0;
            long avgTimeToMergePR = 0;

            for (Event event : events) {
                repoName = event.getRepo().getName();
                if (event.getPayload().getCommits() != null) {
                    numberOfCommits += event.getPayload().getCommits().size();
                } else if (event.getPayload().getPullRequest() != null && event.getPayload().getPullRequest().getMergedDate() != null) {
                    numberOfMergedPR ++;
                    totalHourToMergePR += calculateDuration(event.getPayload().getPullRequest().getCreatedDate(), event.getPayload().getPullRequest().getMergedDate());
                } else if (event.getPayload().getIssue() != null && event.getPayload().getIssue().getCloseDate() != null) {
                    numberOfClosedIssue++;
                    totalHourIssueRemainOpen += calculateDuration(event.getPayload().getIssue().getOpenDate(), event.getPayload().getIssue().getCloseDate());
                }
            }
            if (numberOfClosedIssue != 0) {
                avgTimeIssueRemainOpen = totalHourIssueRemainOpen / numberOfClosedIssue;
            }

            if (numberOfMergedPR != 0) {
                avgTimeToMergePR = totalHourToMergePR / numberOfMergedPR;
            }


            int avgCommitsPerDay = numberOfCommits / numberOfDay;
            RepoStat repoStat = new RepoStat(repoName, numberOfCommits, avgCommitsPerDay, avgTimeIssueRemainOpen, avgTimeToMergePR);
            try {
                writeCSVFile(repoStat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public static long calculateDuration(String startTime, String endTime) {
        LocalDate start = LocalDate.parse(startTime, DateTimeFormatter.ISO_INSTANT);
        LocalDate end =LocalDate.parse(endTime, DateTimeFormatter.ISO_INSTANT);
        Duration duration = Duration.between(end, start);
        return Math.abs(duration.toHours());
    }

    public static void writeCSVFile(RepoStat data) throws IOException {
        String[] headers = {"repo_name","num_commits", "avg_commits_per_day", "avg_time_issue_open", "avg_time_merge_pr"};
        File file = new File("health_scores.csv");
        FileWriter fw;
        if (!file.exists()) {
            fw = new FileWriter(file);
            try (CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT
                    .withHeader(headers))) {
                List<String> Row = new ArrayList<>(Arrays.asList(data.getRepoName(), String.valueOf(data.getNumberOfCommits()), String.valueOf(data.getAvgCommitsPerDay()), String.valueOf(data.getAvgTimeIssueRemainOpened()), String.valueOf(data.getAvgTimeToMergePR())));
                printer.printRecord(Row);
            }

        } else {
            fw = new FileWriter(file, true);
            try (CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT)) {
                List<String> Row = new ArrayList<>(Arrays.asList(data.getRepoName(), String.valueOf(data.getNumberOfCommits()), String.valueOf(data.getAvgCommitsPerDay()), String.valueOf(data.getAvgTimeIssueRemainOpened())));
                printer.printRecord(Row);

            }
        }

    }
}
