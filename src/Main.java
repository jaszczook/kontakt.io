import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {

    private static final String GET = "GET";
    private static final String GIF = "gif";
    private static final int HTTP_200 = 200;

    public static void main(String[] args) {
        String fileName = "hosts_access_log_00.txt";
        Set<String> uniqueGifFilenames = getUniqueGifFilenames(fileName);
        writeToGifsFile(fileName, uniqueGifFilenames);
    }

    private static Set<String> getUniqueGifFilenames(String fileName) {
        final Set<String> gifFilenames = new HashSet<>();

        try (Stream<String> linesStream = Files.lines(Paths.get(fileName))) {
            linesStream.forEach(line -> {
                Pattern quotePattern = Pattern.compile("\"([^\"]*)\"");
                Pattern httpStatusPattern = Pattern.compile("\\s(\\d{3})\\s+\\d+$");
                Matcher quoteMatcher = quotePattern.matcher(line);
                Matcher httpStatusMatcher = httpStatusPattern.matcher(line);

                if (quoteMatcher.find() && httpStatusMatcher.find()) {
                    String request = quoteMatcher.group(1);
                    String[] requestParts = request.split("\\s+");
                    int httpStatusCode = Integer.parseInt(httpStatusMatcher.group(1));
                    File file = new File(requestParts[1]);
                    String filename = file.getName();
                    int dotIndex = filename.lastIndexOf('.');

                    if (GET.equals(requestParts[0]) && // is a GET request
                            httpStatusCode == HTTP_200 && // with HTTP status code 200
                            dotIndex > 0 && // filename contains '.'
                            GIF.equalsIgnoreCase(filename.substring(dotIndex + 1)) // file extension is 'gif' case ignored
                    ) {
                        gifFilenames.add(filename);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file.");
        }

        return gifFilenames;
    }

    private static void writeToGifsFile(String fileName, Set<String> uniqueGifFilenames) {
        Path filePath = Paths.get("gifs_" + fileName);
        try {
            Files.write(filePath, uniqueGifFilenames);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file.");
        }
    }
}
