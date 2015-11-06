package nom.romeara.artifact.repo.versions.exception;

/**
 * Represents an error encountered when collecting authentication information from a user
 *
 * @author romeara
 */
public class AuthenticationException extends Exception {

    /**
     * @param message
     *            Description of the exact issue encountered
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * @param message
     *            Description of the exact issue encountered
     * @param cause
     *            Original exception which lead to an authentication issue
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
