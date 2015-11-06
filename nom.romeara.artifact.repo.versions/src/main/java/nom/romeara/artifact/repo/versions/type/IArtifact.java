package nom.romeara.artifact.repo.versions.type;

/**
 * Representation of an artifact (available dependency)
 *
 * @author romeara
 */
public interface IArtifact {

    /**
     * @return ID of the artifact retrieved
     */
    String getArtifactId();

    /**
     * @return Release ID for the most recent release of the artifact
     */
    String getLatestReleaseId();

    /**
     * @return IDs of all releases available for the artifact
     */
    Iterable<String> getAvailableReleases();

}
