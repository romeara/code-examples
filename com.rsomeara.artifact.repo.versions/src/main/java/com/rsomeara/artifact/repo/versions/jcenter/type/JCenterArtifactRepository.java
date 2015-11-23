package com.rsomeara.artifact.repo.versions.jcenter.type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.rsomeara.artifact.repo.versions.exception.AuthenticationException;
import com.rsomeara.artifact.repo.versions.http.HttpBasicAuthentication;
import com.rsomeara.artifact.repo.versions.type.IArtifact;
import com.rsomeara.artifact.repo.versions.type.IArtifactRelease;
import com.rsomeara.artifact.repo.versions.type.IArtifactRepository;

/**
 * Represents a repository of artifacts on JCenter/BinTray. Handles communication with remote BinTray APIs to retrieve
 * artifact and release information
 *
 * <p>
 * BinTray API documentation can be found <a href="https://bintray.com/docs/api/">here</a>
 * </p>
 *
 * @author romeara
 */
public class JCenterArtifactRepository implements IArtifactRepository {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(JCenterArtifactRepository.class);

    /** Header returned on responses indicating the maximum queries per day a user is allowed */
    private static final String MAXIMUM_DAILY_QUERIES_HEADER = "X-RateLimit-Limit";

    /** Header returned on responses indicating the number of queries of the per day allowance a user has remaining */
    private static final String REMAINING_DAILY_QUERIES_HEADER = "X-RateLimit-Remaining";

    /**
     * Special version identifier which indicates to the BinTray APIs that the client wants information on the most
     * recent release
     */
    private static final String LATEST_VERSION_KEY = "_latest";

    /**
     * Base web address which hosts the BinTray REST APIs this program communicates with. Version 1 is specified to
     * prevent compatibility problems if BinTray releases a new revision
     */
    private static final String BINTRAY_REST_API_URL = "https://bintray.com/api/v1/";

    /** The URL of the repository represented by this Java artifact repository instance */
    private final String repositoryUrl;

    /** Callback handler used if username/API-key authentication is required to access necessary APIs */
    private CallbackHandler callbackHandler;

    /**
     * @param organization
     *            Key of the BinTray organization which owns the represented repository
     * @param repository
     *            Key of the repository to represent
     * @param callbackHandler
     *            Callback handler which handles getting any credentials required. Only used for version-specific
     *            lookups
     */
    private JCenterArtifactRepository(@Nonnull String organization, @Nonnull String repository, @Nonnull CallbackHandler callbackHandler) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(organization) && !organization.trim().isEmpty());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(repository) && !repository.trim().isEmpty());
        Objects.requireNonNull(callbackHandler);

        this.callbackHandler = callbackHandler;
        repositoryUrl = new StringBuilder(BINTRAY_REST_API_URL)
                .append("packages/")
                .append(organization).append('/')
                .append(repository).append('/').toString();
    }

    @Override
    public Optional<IArtifact> findArtifact(String artifactId) {
        IArtifact artifact = null;

        String artifactUrl = new StringBuilder(repositoryUrl).append(artifactId).toString();

        logger.trace("Attempting connection to BinTray APIs at {}", artifactUrl);

        try {
            URLConnection connection = new URL(artifactUrl).openConnection();

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                artifact = JCenterArtifact.create(reader);
            }

            logQueryLimits(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.fromNullable(artifact);
    }

    @Override
    public Optional<IArtifactRelease> findSpecifiedArtifactRelease(String artifactId, String releaseId) {
        return findArtifactVersion(artifactId, releaseId);
    }

    @Override
    public Optional<IArtifactRelease> findLatestArtifactRelease(String artifactId) {
        return findArtifactVersion(artifactId, LATEST_VERSION_KEY);
    }

    /**
     * Looks up an artifact version using a BinTray version ID
     *
     * @param artifactId
     *            The ID of the artifact (in BinTray terms, "package") to look up
     * @param releaseId
     *            The ID of the release (in BinTray terms, "version") to look up. May be a special key which indicates a
     *            version with specific properties
     * @return A representation of an artifact release, if one was found with the given identifiers
     */
    private Optional<IArtifactRelease> findArtifactVersion(String artifactId, String releaseId) {
        IArtifactRelease release = null;

        String releaseUrl = new StringBuilder(repositoryUrl).append(artifactId).append('/').append("versions/").append(releaseId).toString();

        logger.trace("Attempting connection to BinTray APIs at {}", releaseUrl);

        try {
            URLConnection connection = new URL(releaseUrl).openConnection();

            HttpBasicAuthentication authentication = new HttpBasicAuthentication(callbackHandler,
                    new NameCallback("Username: "), new PasswordCallback("API Key: ", false));
            connection = authentication.applyAuthentication(connection);

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                release = JCenterArtifactRelease.create(reader);
            }

            logQueryLimits(connection);
        } catch (IOException | AuthenticationException e) {
            throw new RuntimeException(e);
        }

        return Optional.fromNullable(release);
    }

    /**
     * Logs any returned query limit information to the console
     *
     * @param usedConnection
     *            The connection used to make a query
     */
    private void logQueryLimits(URLConnection usedConnection) {
        String maximumQueries = usedConnection.getHeaderField(MAXIMUM_DAILY_QUERIES_HEADER);
        String remainingQueries = usedConnection.getHeaderField(REMAINING_DAILY_QUERIES_HEADER);

        if (maximumQueries != null && remainingQueries != null) {
            logger.warn("{} daily queries to JCenter APIs remaining of {}", remainingQueries, maximumQueries);
        } else {
            logger.debug("No rate limit information returned by remote call");
        }
    }

    /**
     * Creates a new JCenter artifact repository representation
     * 
     * @param organization
     *            Key of the BinTray organization which owns the represented repository
     * @param repository
     *            Key of the repository to represent
     * @return Representation of the artifact repository at the given location
     */
    public static IArtifactRepository create(@Nonnull String organization, @Nonnull String repository) {
        return new JCenterArtifactRepository(organization, repository, new AuthenticationCallback());
    }

    /**
     * Creates a new JCenter artifact repository representation
     * 
     * @param organization
     *            Key of the BinTray organization which owns the represented repository
     * @param repository
     *            Key of the repository to represent
     * @param callbackHandler
     *            Callback handler which retrieves username and API key data if required for accessed APIs. Must support
     *            NameCallback and PasswordCallback
     * @return Representation of the artifact repository at the given location
     */
    public static IArtifactRepository create(@Nonnull String organization, @Nonnull String repository, @Nonnull CallbackHandler callbackHandler) {
        return new JCenterArtifactRepository(organization, repository, callbackHandler);
    }

    /**
     * Basic callback handler which prompts the user for authentication information via the console each run
     *
     * @author romeara
     */
    private static final class AuthenticationCallback implements CallbackHandler {

        private String username = null;

        private String apiKey = null;

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    NameCallback cb = (NameCallback) callback;

                    if (username == null) {
                        username = getInput(cb.getPrompt());
                    }

                    cb.setName(username);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback cb = (PasswordCallback) callback;

                    if (apiKey == null) {
                        apiKey = getInput(cb.getPrompt());
                    }

                    char[] passwd = new char[apiKey.length()];
                    apiKey.getChars(0, passwd.length, passwd, 0);

                    cb.setPassword(passwd);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }

        private String getInput(String prompt) throws IOException {
            System.out.print(prompt);
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            return in.readLine();
        }

    }

}
