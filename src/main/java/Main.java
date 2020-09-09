import Models.Commit;
import Models.Event;
import Models.RepoStat;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
            // Create object mapper to map json to java object
            ObjectMapper objectMapper = new ObjectMapper();

            // Loop through the json file line by line, and map the json to Event object
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
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
                    System.out.println(e.getMessage());
                    // In case, the a single json is separate into 2 lines,
                    // we read one more line and concatenate them
//                    String line2 = sc.nextLine();
//                    Event event = objectMapper.readValue(line + line2, Event.class);
//                    String repoId = event.getRepo().getId();

//                    if (eventsByRepo.getOrDefault(repoId, null) == null) {
//                        List<Event> events = new ArrayList<>();
//                        events.add(event);
//                        eventsByRepo.put(repoId, events);
//                    } else {
//                        List<Event> events = eventsByRepo.get(repoId);
//                        events.add(event);
//                    }
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


        // Variable to keep track mean max of metric
        int maxAvgCommitsPerDay = 0;
        int maxAvgTimeIssueRemainOpen = 0;
        int maxAvgTimeToMergePR = 0;
        int maxAvgCommitsPerDeveloper = 0;
        int minAvgCommitsPerDay = Integer.MAX_VALUE;
        int minAvgTimeIssueRemainOpen = Integer.MAX_VALUE;
        int minAvgTimeToMergePR = Integer.MAX_VALUE;
        int minAvgCommitsPerDeveloper = Integer.MAX_VALUE;
        List<RepoStat> listOfRepoStat = new ArrayList<>();

        // Loop through all the Events of each repo, and calculate the metric
        for (Map.Entry<String, List<Event>> entry : eventsByRepo.entrySet()) {
            String id = entry.getKey();
            List<Event> events = entry.getValue();
            String repoName = "";
            int numberOfCommits = 0;

            int numberOfClosedIssue = 0;
            long totalHourIssueRemainOpen = 0;
            long avgTimeIssueRemainOpen = 0;

            int numberOfMergedPR = 0;
            int totalHourToMergePR = 0;
            long avgTimeToMergePR = 0;

            Set<String> setOfDevelopers = new HashSet<>();
            int avgCommitsPerDeveloper = 0;


            for (Event event : events) {
                repoName = event.getRepo().getName();
                if (event.getPayload().getCommits() != null) {
                    List<Commit> listOfCommits = event.getPayload().getCommits();
                    numberOfCommits += listOfCommits.size();
                    for (Commit commit : listOfCommits) {
                        setOfDevelopers.add(commit.getAuthor().getEmail());
                    }
                } else if (event.getPayload().getPullRequest() != null && event.getPayload().getPullRequest().getMergedDate() != null) {
                    numberOfMergedPR++;
                    totalHourToMergePR += calculateDuration(event.getPayload().getPullRequest().getCreatedDate(), event.getPayload().getPullRequest().getMergedDate());
                } else if (event.getPayload().getIssue() != null && event.getPayload().getIssue().getCloseDate() != null) {
                    numberOfClosedIssue++;
                    totalHourIssueRemainOpen += calculateDuration(event.getPayload().getIssue().getOpenDate(), event.getPayload().getIssue().getCloseDate());
                }
            }

            // METRIC 1: average number of commits per day
            int avgCommitsPerDay = numberOfCommits / numberOfDay;

            // METRIC 2: avg time issue remained open
            if (numberOfClosedIssue != 0) {
                avgTimeIssueRemainOpen = totalHourIssueRemainOpen / numberOfClosedIssue;
            }

            // METRIC 3: avg time to merge PR
            if (numberOfMergedPR != 0) {
                avgTimeToMergePR = totalHourToMergePR / numberOfMergedPR;
            }

            // METRIC 4: Ratio of commit per developers
            if (setOfDevelopers.size() != 0) {
                avgCommitsPerDeveloper = numberOfCommits / setOfDevelopers.size();
            }

            // Set min max metric
            int[] minMaxValues = setMinMaxValue(new int[]{maxAvgCommitsPerDay, minAvgCommitsPerDay}, avgCommitsPerDay);
            maxAvgCommitsPerDay = minMaxValues[0];
            minAvgCommitsPerDay = minMaxValues[1];
            minMaxValues = setMinMaxValue(new int[]{maxAvgTimeIssueRemainOpen, minAvgTimeIssueRemainOpen}, (int) avgTimeIssueRemainOpen);
            maxAvgTimeIssueRemainOpen = minMaxValues[0];
            minAvgTimeIssueRemainOpen = minMaxValues[1];
            minMaxValues = setMinMaxValue(new int[]{maxAvgTimeToMergePR, minAvgTimeToMergePR}, (int) avgTimeToMergePR);
            maxAvgTimeToMergePR = minMaxValues[0];
            minAvgTimeToMergePR = minMaxValues[1];
            minMaxValues = setMinMaxValue(new int[]{maxAvgCommitsPerDeveloper, minAvgCommitsPerDeveloper}, avgCommitsPerDeveloper);
            maxAvgCommitsPerDeveloper = minMaxValues[0];
            minAvgCommitsPerDeveloper = minMaxValues[1];


            RepoStat repoStat = new RepoStat(repoName, numberOfCommits, avgCommitsPerDay, avgTimeIssueRemainOpen, avgTimeToMergePR, avgCommitsPerDeveloper);
            listOfRepoStat.add(repoStat);
//            try {
//                writeCSVFile(repoStat);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        // Normalize and calculate health
        for (RepoStat repoStat: listOfRepoStat) {
            float repoHealth = normalizeValue(minAvgCommitsPerDay, maxAvgCommitsPerDay, repoStat.getAvgCommitsPerDay(), false) +
                    normalizeValue(minAvgTimeIssueRemainOpen, maxAvgTimeIssueRemainOpen, (int) repoStat.getAvgTimeIssueRemainOpened(), true) +
                    normalizeValue(minAvgTimeToMergePR, maxAvgTimeToMergePR, (int) repoStat.getAvgTimeToMergePR(), true) +
                    normalizeValue(minAvgCommitsPerDeveloper, maxAvgCommitsPerDeveloper, repoStat.getAvgCommitPerDeveloper(), false);

            repoStat.setHealth(repoHealth);
        }

        // sort the list
        listOfRepoStat.sort(Comparator.comparing(RepoStat::getHealth));

    }


    public static float normalizeValue(int minValue, int maxValue, int value, boolean isTime) {
        float newValue = (float)(value - minValue) / (float)(maxValue - minValue);
        if (isTime) return 1 - newValue;
        return  newValue;
    }

    public static int[] setMinMaxValue(int[] currentMinMaxValue, int value) {
        if (value >  currentMinMaxValue[0]) {
            currentMinMaxValue[0] = value;
        }
        if (value < currentMinMaxValue[1]) {
            currentMinMaxValue[1] = value;
        }

        return currentMinMaxValue;
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
