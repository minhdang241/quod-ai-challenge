package ai.quod.challenge.Helpers;

import ai.quod.challenge.Models.Event;
import ai.quod.challenge.Models.RepoStat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataProcessUtils {
    private static final Logger LOGGER = Logger.getLogger(DataProcessUtils.class.getName());

    public static float normalizeValue(List<Float> minMaxValue, float currentValue, boolean isTime) {
        float maxValue = minMaxValue.get(0);
        float minValue = minMaxValue.get(1);
        if (minValue == 0  && maxValue == 0) return 0;
        float newValue = (currentValue - minValue) / (maxValue - minValue);
        if (isTime) return 1 - newValue;
        return  newValue;
    }


    public static List<Float> updateMinMaxValue(List<Float> currentMinMaxValue, float value) {
        if (value >  currentMinMaxValue.get(0)) {
            currentMinMaxValue.set(0, value);
        }
        if (value < currentMinMaxValue.get(1)) {
            currentMinMaxValue.set(1, value);
        }
        return currentMinMaxValue;
    }


    public static void extractEventsFromFile(Map<String, List<Event>> eventsByRepo, Path file) {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(file.toString());
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

                } catch (JsonProcessingException e) {
                    // If cannot convert to Event object => write the record to the error file
                    File errorFile = new File("error_file.txt");
                    FileWriter fw;
                    try {
                        if (!errorFile.exists()) {
                            fw = new FileWriter(errorFile);
                        } else {
                            fw = new FileWriter(errorFile, true);
                        }
                        System.out.println(line);
                        fw.write(line);
                        fw.close();
                        LOGGER.log(Level.INFO, "Cannot convert to Event object");
                    } catch (IOException err) {
                        LOGGER.log(Level.WARNING, "Cannot write to error_file.txt");
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
    }


    public static int calculateDuration(String startTime, String endTime) {
        LocalDate start = LocalDate.parse(startTime, DateTimeFormatter.ISO_INSTANT);
        LocalDate end =LocalDate.parse(endTime, DateTimeFormatter.ISO_INSTANT);
        Duration duration = Duration.between(end, start);
        return (int) Math.abs(duration.toHours());
    }


    public static void writeCSVFile(RepoStat data) throws IOException {
        String[] headers = {"repo_name","health_score", "num_commits", "commits_per_day", "time_issue_open", "time_merge_pr", "commits_per_dev"};
        File file = new File("health_scores.csv");
        FileWriter fw;
        if (!file.exists()) {
            fw = new FileWriter(file);
            try (CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT
                    .withHeader(headers))) {
                writeRecord(data, printer);
            }
        } else {
            fw = new FileWriter(file, true);
            try (CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT)) {
                writeRecord(data, printer);
            }
        }
    }


    private static void writeRecord(RepoStat data, CSVPrinter printer) throws IOException {
        List<String> Row = new ArrayList<>(Arrays.asList(data.getRepoName(), String.valueOf(data.getHealth()), String.valueOf(data.getNumberOfCommits()),
                String.valueOf(data.getAvgCommitsPerDay()), String.valueOf(data.getAvgTimeIssueRemainOpened()), String.valueOf(data.getAvgTimeToMergePR()),
                String.valueOf(data.getAvgCommitPerDeveloper())));
        printer.printRecord(Row);
    }
}
