package nom.romeara.query.construction.postgresql.type;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.postgresql.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility functionality around handling data passed to PostgreSQL
 *
 * @author romeara
 * @since 0.1
 */
public final class Values {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(Values.class);

    private static final Function<String, String> ESCAPE_SPECIAL_CHARACTERS = new SpecialCharacterEscapeFunction();

    private static final Function<String, String> PROTECT_STRING = ESCAPE_SPECIAL_CHARACTERS
            .andThen((String t) -> t != null ? new StringBuilder().append('\'').append(t).append('\'').toString() : null);

    /**
     * Prevent instantiation of utility class
     */
    private Values() throws InstantiationException {
        throw new InstantiationException("Cannot instantiate instance of utility class '" + getClass().getName() + "'");
    }

    /**
     * @return Function which will escape special characters in data values
     * @since 0.1
     */
    @Nonnull
    public static Function<String, String> escapeSpecialCharacters() {
        return ESCAPE_SPECIAL_CHARACTERS;
    }

    /**
     * @return Function which will escape special characters in data values and surround each value with ', causing them
     *         to be treated as string literals
     * @since 0.1
     */
    @Nonnull
    public static Function<String, String> protectString() {
        return PROTECT_STRING;
    }

    /**
     * @param input
     *            A standard Java function which a values function compatible signature
     * @return A values function representation of the provided function
     * @since 0.1
     */
    @Nonnull
    public static <T> ValuesFunction<T> asValuesFunction(@Nonnull Function<T, List<String>> input) {
        Objects.requireNonNull(input);

        return ((T t) -> input.apply(t));
    }

    /**
     * @return Values function which will escape special characters in data values and surround each value with ',
     *         causing them to be treated as string literals
     * @since 0.1
     */
    @Nonnull
    public static ValuesFunction<String> protectedStrings() {
        return ((String t) -> (t != null ? Arrays.asList(protectString().apply(t)) : null));
    }

    /**
     * Function which escapes special characters in SQL data values for PostgreSQL
     *
     * @author romeara
     */
    private static final class SpecialCharacterEscapeFunction implements Function<String, String> {

        @Override
        public String apply(String input) {
            String result = null;

            if (input != null) {
                try {
                    result = Utils.appendEscapedLiteral(null, input, true).toString();
                } catch (SQLException e) {
                    logger.error("Error escaping query string - string contained a '\\0' chracter", e);
                }
            }

            return result;
        }

    }

}
