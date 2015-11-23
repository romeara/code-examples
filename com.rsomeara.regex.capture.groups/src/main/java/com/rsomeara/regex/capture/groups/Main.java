package com.rsomeara.regex.capture.groups;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application which reads a file off its class-path and applies a regex search to the contents
 *
 * @author romeara
 */
public class Main {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /** The location of the file to look up relative to the class-path root */
    private static final String SOURCE_FILE_LOCATION = "source-files" + File.separator + "source1.txt";

    public static void main(String[] args) throws Exception {
        new Main().doMain(args);
    }

    public void doMain(String[] args) throws Exception {
        try (BufferedReader source1Reader = getFileReader(SOURCE_FILE_LOCATION)) {

            Pattern pattern = getRegexPattern();

            String line = source1Reader.readLine();

            while (line != null) {
                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    logger.info("Statement: {}", matcher.group(0));

                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        logger.info("\tGroup {}: {}", i, matcher.group(i));
                    }
                }

                line = source1Reader.readLine();
            }
        }
    }

    /**
     * Creates a reader for a file on the class-path of this application. This method will still work once this
     * application is packaged within a jar archive
     *
     * @param classpathLocation
     *            The location relative to the class-path root where the file can be found
     * @return A reader for the file
     */
    private BufferedReader getFileReader(String classpathLocation) {
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(classpathLocation);

        return new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * @return A regex pattern with capturing and non-capturing groups
     */
    private Pattern getRegexPattern() {
        // The first pattern includes some sub-groups (denoted by "()"), which we don't want returned as a "group". So
        // we add the "?:" at the beginning within the "()" for the group, which excludes it. By leaving it out of the
        // outermost group, that sub-string of the match will be returned as an individual group
        // We also add the "?:" to the entire group in the second pattern, preventing its capture at all. All groups,
        // regardless of capture setting, must still be matched for the pattern to match
        String capturingPattern = "((?:(?:[A-Z][\\w]*)(?: ))+)";
        String nonCapturingPattern = "(?:(?:(?:[\\w]*)(?: |))+)";

        return Pattern.compile(new StringBuilder(capturingPattern).append(nonCapturingPattern).toString());
    }

}
