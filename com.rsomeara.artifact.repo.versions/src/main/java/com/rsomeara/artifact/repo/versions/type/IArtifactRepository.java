package com.rsomeara.artifact.repo.versions.type;

import com.google.common.base.Optional;

/**
 * Representation of a repository which provides artifacts for dependency management systems
 *
 * @author romeara
 */
public interface IArtifactRepository {

    /**
     * Retrieves information on the specified artifact
     * 
     * @param artifactId
     *            ID of the artifact
     * @return Representation of the specified artifact, or an absent optional if the information could not be retrieved
     */
    Optional<IArtifact> findArtifact(String artifactId);

    /**
     * Retrieves information on the specified release for the given artifact
     *
     * @param artifactId
     *            ID of the artifact
     * @param releaseId
     *            ID of the release
     * @return Representation of the specified release, or an absent optional if information could not be retrieved
     */
    Optional<IArtifactRelease> findSpecifiedArtifactRelease(String artifactId, String releaseId);

    /**
     * Retrieves information on the most recent available for the given artifact
     *
     * @param artifactId
     *            ID of the artifact
     * @return Representation of the latest release, or an absent optional if information could not be retrieved
     */
    Optional<IArtifactRelease> findLatestArtifactRelease(String artifactId);
}
