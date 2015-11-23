package com.rsomeara.artifact.repo.versions.type;

/**
 * Representation of a specific artifact release
 *
 * @author romeara
 */
public interface IArtifactRelease {

    /**
     * @return ID of the artifact the release belongs to
     */
    String getArtifactId();

    /**
     * @return ID of the release retrieved
     */
    String getReleaseId();

}
