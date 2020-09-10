package ai.quod.challenge.Helpers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Utils {

    public static LocalDateTime convertIS8061DateTime(String datetime) {
        DateTimeFormatter ISO8601Formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
                .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
                .optionalStart().appendOffset("+HH", "Z").optionalEnd()
                .toFormatter();
        return LocalDateTime.parse(datetime, ISO8601Formatter);
    }

    public static void downloadWithApacheCommons(String url, String localFilename) {
        int CONNECT_TIMEOUT = 10000;
        int READ_TIMEOUT = 10000;
        try {
            FileUtils.copyURLToFile(new URL(url), new File(localFilename), CONNECT_TIMEOUT, READ_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void decompressGzip(Path source, Path target) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(
                new FileInputStream(source.toFile()));
             FileOutputStream fos = new FileOutputStream(target.toFile())) {
            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

        }
    }

    public static List<String> generateURLForEachHour(String url, LocalDateTime startDate, LocalDateTime endDate) {
        List<String> urls = new ArrayList<>();
        StringBuilder generatedURL = new StringBuilder(url);
        while (!startDate.isAfter(endDate)) {
            generatedURL.append(startDate.getYear()).append("-");
            if (startDate.getMonthValue() < 10) {
                generatedURL.append("0");
            }
            generatedURL.append(startDate.getMonthValue()).append("-");
            if (startDate.getDayOfMonth() < 10) {
                generatedURL.append("0");
            }
            generatedURL.append(startDate.getDayOfMonth()).append("-").append(startDate.getHour()).append(".json.gz");

            urls.add(generatedURL.toString());
            // convert the url to the original one, ready for the next generatedURL
            generatedURL.setLength(url.length());
            // Update startDate
            startDate = startDate.plusHours(1);
        }
        return urls;
    }
}
