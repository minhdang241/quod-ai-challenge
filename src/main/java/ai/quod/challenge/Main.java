package ai.quod.challenge;
import ai.quod.challenge.Models.Commit;
import ai.quod.challenge.Models.Event;
import ai.quod.challenge.Models.RepoStat;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static ai.quod.challenge.Helpers.DataProcessUtils.*;
import static ai.quod.challenge.Helpers.GeneralUtils.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        /*
            THE MAIN FUNCTION FLOW THE STEPS BELOW:
            Step 1: Get datetime from arguments, then generate download url for each hour based on that
            Step 2: Parsing the downloaded files line by line -> convert to event objects -> store those objects in the Map along with the repo Id
            Step 3: Calculate metrics using those events object -> normalize them -> calculate health of each repo -> write them to health_scores.txt
        **/

        int numberOfDay = 1; // use this number to calculate the average commit per day

        List<String> urls;

        // STEP 1: Get datetime from arguments, then generate download url for each hour based on that
        if (args.length < 2) {
            LOGGER.log(Level.WARNING, "Require 2 arguments");
            return;
        } else {
            System.out.println(args[0] + " " + args[1]);
            LocalDateTime startDate;
            try {
                startDate = convertIS8061DateTime(args[0]);
            } catch (DateTimeParseException e) {
                LOGGER.log(Level.WARNING, "The first argument has the wrong format");
                return;
            }
            LocalDateTime endDate;
            try {
                 endDate = convertIS8061DateTime(args[1]);
            } catch (DateTimeParseException e) {
                LOGGER.log(Level.WARNING, "The second argument has the wrong format");
                return;
            }

            // generate downloaded URL
            urls = generateURLForEachHour("https://data.gharchive.org/", startDate, endDate);
            numberOfDay = (int)Duration.between(startDate, endDate).toDays();

            // If numberOfDay is 0, set it to 1 to avoid NaN value when calculating the average commits per day
            if (numberOfDay < 1) {
                numberOfDay = 1;
            }

        }

        //STEP 2: Parsing the downloaded files line by line -> convert to event objects -> store those objects in the Map along with the repo Id
        // Create a hash map to store events in each file
        Map<String, List<Event>> eventsByRepo  = new HashMap<>();
        // Loop through each url -> download the file -> extract the information -> delete files to save storage
        for (String url: urls) {
            downloadFileFromURL(url, "data.json.gz");
            Path source = Paths.get("data.json.gz");
            Path target = Paths.get("data.json");

            if (Files.notExists(source)) {
                System.err.printf("The path %s doesn't exist!", source);
                return;
            }
            try {
                // 1. extract the file from gz extension to json extension
                decompressGzip(source, target);
                // 2. extract events from file and save them to the eventsByRepo map
                extractEventsFromFile(eventsByRepo, target);
                LOGGER.log(Level.INFO, "Extract events from " + url);
                // 3. Delete files after using to save the storage
                Files.delete(source);
                Files.delete(target);

            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot not extract events from " + url);
            }
        }

        // STEP 3: Calculate metrics using those events object -> normalize them -> calculate health of each repo -> write them to health_scores.txt
        Map<String, List<Float>> minMaxValue = new HashMap<>();
        minMaxValue.put("avgCommitsPerDay", new ArrayList<>(Arrays.asList((float) 0, Float.MAX_VALUE)));
        minMaxValue.put("avgTimeIssueRemainOpen", new ArrayList<>(Arrays.asList((float) 0, Float.MAX_VALUE)));
        minMaxValue.put("avgTimeToMergePR", new ArrayList<>(Arrays.asList((float) 0, Float.MAX_VALUE)));
        minMaxValue.put("avgCommitsPerDeveloper", new ArrayList<>(Arrays.asList((float) 0, Float.MAX_VALUE)));
        List<RepoStat> listOfRepoStat = new ArrayList<>();

        // Loop through all the Events of each repo, and calculate the metric
        for (Map.Entry<String, List<Event>> entry : eventsByRepo.entrySet()) {
            String id = entry.getKey();
            List<Event> events = entry.getValue();
            String repoName = "";
            int numberOfCommits = 0;

            int numberOfClosedIssue = 0;
            int totalHourIssueRemainOpen = 0;
            float avgTimeIssueRemainOpen = 0;

            int numberOfMergedPR = 0;
            int totalHourToMergePR = 0;
            float avgTimeToMergePR = 0;

            Set<String> setOfDevelopers = new HashSet<>();
            int avgCommitsPerDeveloper = 0;
            // Loop through all the events to calculate the metrics
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
            float avgCommitsPerDay = (float)numberOfCommits / numberOfDay;

            // METRIC 2: avg time issue remained open
            if (numberOfClosedIssue != 0) {
                avgTimeIssueRemainOpen = (float)totalHourIssueRemainOpen / numberOfClosedIssue;
            }

            // METRIC 3: avg time to merge PR
            if (numberOfMergedPR != 0) {
                avgTimeToMergePR = (float)totalHourToMergePR / numberOfMergedPR;
            }

            // METRIC 4: Ratio of commit per developers
            if (setOfDevelopers.size() != 0) {
                avgCommitsPerDeveloper = numberOfCommits / setOfDevelopers.size();
            }

            for (Map.Entry<String, List<Float>> element : minMaxValue.entrySet()) {
                List<Float> newMinMaxValue = updateMinMaxValue(element.getValue(), avgCommitsPerDay);
                element.setValue(newMinMaxValue);
            }

            RepoStat repoStat = new RepoStat(repoName, numberOfCommits, avgCommitsPerDay, avgTimeIssueRemainOpen, avgTimeToMergePR, avgCommitsPerDeveloper);

            listOfRepoStat.add(repoStat);
        }


        // Normalize and calculate health
        List<Float> minMaxHealthValue = new ArrayList<>(Arrays.asList((float) 0, Float.MAX_VALUE));
        for (RepoStat repoStat: listOfRepoStat) {
            repoStat.setAvgCommitsPerDay(normalizeValue(minMaxValue.get("avgCommitsPerDay"), repoStat.getAvgCommitsPerDay(), false));
            repoStat.setAvgTimeIssueRemainOpened(normalizeValue(minMaxValue.get("avgTimeIssueRemainOpen"), repoStat.getAvgTimeIssueRemainOpened(), true));
            repoStat.setAvgTimeToMergePR(normalizeValue(minMaxValue.get("avgTimeToMergePR"), repoStat.getAvgTimeToMergePR(), true));
            repoStat.setAvgCommitPerDeveloper(normalizeValue(minMaxValue.get("avgCommitsPerDeveloper"), repoStat.getAvgCommitPerDeveloper(), false));
            float repoHealth = repoStat.getAvgCommitsPerDay() + repoStat.getAvgTimeIssueRemainOpened() + repoStat.getAvgTimeToMergePR() + repoStat.getAvgCommitPerDeveloper();
            repoStat.setHealth(repoHealth);

            // Update current min/max health values
            updateMinMaxValue(minMaxHealthValue, repoHealth);
        }

        // Normalize the health
        for (RepoStat repoStat: listOfRepoStat) {
            repoStat.setHealth(normalizeValue(minMaxHealthValue, repoStat.getHealth(), false));
        }

        // sort the list
        listOfRepoStat.sort(Comparator.comparing(RepoStat::getHealth).reversed());

        // Write the first 1000 repos to health_score
        int index = 0;
        for (RepoStat repoStat: listOfRepoStat) {
            if (index > 999) {
                break;
            }
            try {
                writeCSVFile(repoStat);
                index++;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot write to csv file");
                e.printStackTrace();
            }
        }
    }

}
