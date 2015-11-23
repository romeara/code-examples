package com.rsomeara.artifact.repo.versions.jcenter.type;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.rsomeara.artifact.repo.versions.type.AbstractContentHandler;
import com.rsomeara.artifact.repo.versions.type.IArtifactRelease;

/**
 * Representation of an artifact release retrieved from a JCenter/BinTray repository. Handles converting from return of
 * JCenter/BinTray APIs to a Java representation
 *
 * <p>
 * See <a href="https://bintray.com/docs/api/#_get_version">API documentation</a> for a full description of fields
 * returned in JSON form from REST
 * </p>
 *
 * @author romeara
 */
public class JCenterArtifactRelease implements IArtifactRelease {

    private final String artifactId;

    private final String releaseId;

    /**
     * @param json
     *            Content handler which has traversed returned JSON and found interesting data entries
     */
    private JCenterArtifactRelease(JsonContentHandler json) {
        Objects.requireNonNull(json.getArtifactId());
        Objects.requireNonNull(json.getReleaseId());

        artifactId = json.getArtifactId();
        releaseId = json.getReleaseId();
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getReleaseId() {
        return releaseId;
    }

    /**
     * Creates an artifact release representation from JSON retrieved from JCenter/BinTray APIs
     * <p>
     * See <a href="https://bintray.com/docs/api/#_get_version">API documentation</a> for a full description of API use
     * expected
     * </p>
     *
     * @param json
     *            The JSON read from JCenter/BinTray
     * @return An artifact release representation from the provided JSON, or null if the representation provided did not
     *         meet expected patterns
     */
    public static IArtifactRelease create(Reader json) {
        IArtifactRelease result = null;

        JsonContentHandler contentHandler = new JsonContentHandler();
        JSONParser parser = new JSONParser();

        try {
            parser.parse(json, contentHandler, false);

            // If no artifact ID, we didn't get the expected JSON back - got a "no such version" message
            if (contentHandler.getArtifactId() != null && contentHandler.getReleaseId() != null) {
                result = new JCenterArtifactRelease(contentHandler);
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

        private static final String ARTIFACT_ID_KEY = "package";

        private static final String RELEASE_ID_KEY = "name";

        private String artifactId = null;

        private String releaseId = null;

        /**
         * @return The artifact ID read
         */
        public String getArtifactId() {
            return artifactId;
        }

        /**
         * @return The release ID read
         */
        public String getReleaseId() {
            return releaseId;
        }

        @Override
        public boolean primitive(Object value) throws ParseException, IOException {
            if (Objects.equals(getCurrentKey(), ARTIFACT_ID_KEY)) {
                artifactId = (value != null ? value.toString() : "");
            } else if (Objects.equals(getCurrentKey(), RELEASE_ID_KEY)) {
                releaseId = (value != null ? value.toString() : "");
            }

            return !isComplete();
        }

        @Override
        protected boolean isComplete() {
            return artifactId != null && releaseId != null;
        }

    }

}
