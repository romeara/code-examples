package nom.romeara.artifact.repo.versions.jcenter.type;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;

import nom.romeara.artifact.repo.versions.type.AbstractContentHandler;
import nom.romeara.artifact.repo.versions.type.IArtifact;

/**
 * Representation of an artifact retrieved from a JCenter/BinTray repository. Handles converting from return of
 * JCenter/BinTray APIs to a Java representation
 *
 * <p>
 * See <a href="https://bintray.com/docs/api/#_get_package">API documentation</a> for a full description of fields
 * returned in JSON form from REST
 * </p>
 *
 * @author romeara
 */
public class JCenterArtifact implements IArtifact {

    private final String artifactId;

    private final String latestReleaseId;

    private final Iterable<String> availableReleases;

    /**
     * @param json
     *            Content handler which has traversed returned JSON and found interesting data entries
     */
    private JCenterArtifact(JsonContentHandler json) {
        Objects.requireNonNull(json.getArtifactId());

        artifactId = json.getArtifactId();
        latestReleaseId = json.getLatestReleaseId();
        availableReleases = (json.getAvailableReleases() != null ? json.getAvailableReleases() : Collections.EMPTY_LIST);
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getLatestReleaseId() {
        return latestReleaseId;
    }

    @Override
    public Iterable<String> getAvailableReleases() {
        return availableReleases;
    }

    // TODO Object methods

    /**
     * Creates an artifact representation from JSON retrieved from JCenter/BinTray APIs
     * <p>
     * See <a href="https://bintray.com/docs/api/#_get_package">API documentation</a> for a full description of API use
     * expected
     * </p>
     *
     * @param json
     *            The JSON read from JCenter/BinTray
     * @return An artifact representation from the provided JSON, or null if the representation provided did not meet
     *         expected patterns
     */
    public static IArtifact create(Reader json) {
        IArtifact result = null;

        JsonContentHandler contentHandler = new JsonContentHandler();
        JSONParser parser = new JSONParser();

        try {
            parser.parse(json, contentHandler, false);

            // If no artifact ID, we didn't get the expected JSON back - got a "no such artifact" message
            if (contentHandler.getArtifactId() != null) {
                result = new JCenterArtifact(contentHandler);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * Content handler implementation which looks for specific data keys in a JSON stream and reads their values. Once
     * all keys have been found, the handler indicates to the parser it is done
     *
     * @author romeara
     */
    private static final class JsonContentHandler extends AbstractContentHandler {

        private static final String ARTIFACT_ID_KEY = "name";

        private static final String AVAILABLE_RELEASES_KEY = "versions";

        private static final String LATEST_RELEASE_KEY = "latest_version";

        private String artifactId = null;

        private String latestReleaseId = null;

        private List<String> availableReleases = null;

        /**
         * @return The artifact ID read
         */
        public String getArtifactId() {
            return artifactId;
        }

        /**
         * @return The release ID of the most recent release
         */
        public String getLatestReleaseId() {
            return latestReleaseId;
        }

        /**
         * @return Information found on the releases available for the artifact
         */
        public Iterable<String> getAvailableReleases() {
            return availableReleases;
        }

        @Override
        public boolean primitive(Object value) throws ParseException, IOException {
            if (Objects.equals(getCurrentKey(), ARTIFACT_ID_KEY)) {
                artifactId = (value != null ? value.toString() : "");
            } else if (Objects.equals(getCurrentKey(), LATEST_RELEASE_KEY)) {
                latestReleaseId = (value != null ? value.toString() : "");
            } else if (Objects.equals(getCurrentKey(), AVAILABLE_RELEASES_KEY)) {
                String v = (value != null ? value.toString() : "");

                if (v != null) {
                    if (availableReleases == null) {
                        availableReleases = Lists.newLinkedList();
                    }
                    availableReleases.add(v);
                }
            }

            return !isComplete();
        }

        @Override
        protected boolean isComplete() {
            // Until we move past versions, we might still be midway through parsing them
            return artifactId != null && latestReleaseId != null && availableReleases != null && !Objects.equals(getCurrentKey(), AVAILABLE_RELEASES_KEY);
        }

    }

}
