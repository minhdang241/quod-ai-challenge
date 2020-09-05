import Models.Events.Event;
import Models.RepoStat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
        Map<String, RepoStat> stats = new HashMap<>();
        String[] HEADERS = {"repo_name","num_commits"};

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
            inputStream = new FileInputStream("2015-01-01-15.json");
            sc = new Scanner(inputStream, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
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
            for (Event event : events) {
                repoName = event.getRepo().getName();
                if (event.getPayload().getCommits() != null) {
                    numberOfCommits += event.getPayload().getCommits().size();
                } else if (event.getPayload().getIssue() != null && event.getPayload().getIssue().getCloseDate() != null) {
                        numberOfClosedIssue++;
                        LocalDate openTime = LocalDate.parse(event.getPayload().getIssue().getOpenDate(), DateTimeFormatter.ISO_INSTANT);
                        LocalDate closeTime =LocalDate.parse(event.getPayload().getIssue().getCloseDate(), DateTimeFormatter.ISO_INSTANT);
                        Duration duration = Duration.between(closeTime, openTime);
                        totalHourIssueRemainOpen += duration.toHours();
                }
            }
            if (numberOfClosedIssue != 0) {
                avgTimeIssueRemainOpen = totalHourIssueRemainOpen / numberOfClosedIssue;
            }

            int avgCommitsPerDay = numberOfCommits / numberOfDay;
            RepoStat repoStat = new RepoStat(repoName, numberOfCommits, avgCommitsPerDay, avgTimeIssueRemainOpen);
            try {
                writeCSVFile(repoStat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }


//    public static Map<String, RepoStat>  loadRecordFromFile() {
//        Map<String, RepoStat> stats = new HashMap<>();
//        try {
//            Reader in = new FileReader("health_scores.csv");
//            Iterable<CSVRecord> records = CSVFormat.DEFAULT
//                    .withFirstRecordAsHeader().parse(in);
//            for (CSVRecord record : records) {
//                String repoName = record.get("repo_name");
//
//                int numOfCommits = Integer.parseInt(record.get("num_commits"));
//
//                /* Check if the repo exists in the map
//                   - If it exists, increment the number of commits
//                   - If not, create a new RepoStat object and put it into the map
//                **/
//                if (stats.getOrDefault(repoName, null) != null) {
//
//                    RepoStat repoStat = stats.get(repoName);
//                    repoStat.incrementCommit(numOfCommits);
//                } else  {
//                    RepoStat repoStat = new RepoStat(repoName, numOfCommits);
//                    stats.put(repoName, repoStat);
//                }
//
//            }
//        } catch (IOException error) {
//            error.getStackTrace();
//            System.out.println(error.getMessage());
//        }
//        return stats;
//    }

    public static Map<String, String> extractData(JsonParser jsonParser, String attribute) throws IOException{
        Map<String, String> data = new HashMap<>();
        while (jsonParser.nextToken() != null) {

            switch (jsonParser.getText()) {
                case "repo":
                    data.put("repo_name", getValue(jsonParser, "name"));
                    data.put("repo_id", getValue(jsonParser, "id"));
                    break;
                case "commit":
                    data.put("num_commits", Integer.toString(getCommits(jsonParser)));
                    return data;
                default:
            }
        }

        return data;
    }

    public static String getValue(JsonParser jsonParser, String keyValue) throws IOException{
        while (jsonParser.nextToken() != null) {
            if ((keyValue).equals(jsonParser.getCurrentName())) {
                jsonParser.nextToken();
                return jsonParser.getValueAsString();
            }
        }
        return "";
    }

    private static int getCommits(JsonParser jsonParser) throws IOException {
        int numOfCommits = 0;
        while (jsonParser.nextToken() != null && jsonParser.currentToken() != JsonToken.END_ARRAY) {
            if (jsonParser.getCurrentName() == null) {
                numOfCommits++;
            }
        }
        return numOfCommits / 2;
    }



    public static List<String> extractIssueDateTime(JsonParser jsonParser) {
        return new ArrayList<>();
    }




    public static void writeCSVFile(RepoStat data) throws IOException {
        String[] headers = {"repo_name","num_commits", "avg_commits_per_day", "avg_time_issue_remain_open"};
        File file = new File("health_scores.csv");
        FileWriter fw;
        if (!file.exists()) {
            fw = new FileWriter(file);
            try (CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT
                    .withHeader(headers))) {
                List<String> Row = new ArrayList<>(Arrays.asList(data.getRepoName(), String.valueOf(data.getNumberOfCommits()), String.valueOf(data.getAvgCommitsPerDay()), String.valueOf(data.getAvgTimeIssueRemainOpened())));

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
