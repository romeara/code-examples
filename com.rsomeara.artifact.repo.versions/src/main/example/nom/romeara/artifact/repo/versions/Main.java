package nom.romeara.artifact.repo.versions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import nom.romeara.artifact.repo.versions.jcenter.type.JCenterArtifactRepository;
import nom.romeara.artifact.repo.versions.type.IArtifact;
import nom.romeara.artifact.repo.versions.type.IArtifactRelease;
import nom.romeara.artifact.repo.versions.type.IArtifactRepository;

/**
 * Simple example of library use which retrieves the log4j artifact and latest release from BinTray/JCenter
 *
 * @author romeara
 */
public class Main {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String ORGANIZATION_ID = "bintray";

    private static final String REPOSITORY_ID = "jcenter";

    private static final String ARTIFACT_ID = "org.apache.logging.log4j:log4j";

    public static void main(String[] args) {
        new Main().doMain(args);
    }

    public void doMain(String[] args) {
        try {
            IArtifactRepository artifactRepository = JCenterArtifactRepository.create(ORGANIZATION_ID, REPOSITORY_ID);

            Optional<IArtifact> artifact = artifactRepository.findArtifact(ARTIFACT_ID);

            if (artifact.isPresent()) {
                logger.info("Latest Release: {}", artifact.get().getLatestReleaseId());

                for (String version : artifact.get().getAvailableReleases()) {
                    logger.info("\tAvailable Release: {}", version);
                }

                // Demonstrates how the library handles APIs which require API keys (JCenter API Keys are found in User
                // Profile -> Edit -> API Key)
                Optional<IArtifactRelease> latestRelease = artifactRepository.findLatestArtifactRelease(ARTIFACT_ID);

                if (latestRelease.isPresent()) {
                    logger.info("Latest Release: {}", latestRelease.get().getReleaseId());
                } else {
                    logger.error("No \"latest\" release found");
                }
            } else {
                logger.error("Did not find expected artifact");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
