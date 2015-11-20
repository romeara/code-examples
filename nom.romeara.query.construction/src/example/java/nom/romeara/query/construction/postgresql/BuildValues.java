package nom.romeara.query.construction.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nom.romeara.query.construction.postgresql.type.Values;

/**
 * Simple values statement building example. Takes all arguments and converts them into a 1-column values statement
 * representation
 *
 * @author romeara
 * @since 0.1
 */
public class BuildValues {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(BuildValues.class);

    /**
     * Takes input arguments and converts them to an example values statement
     *
     * @param args
     *            Set of strings to represent as a 1-column values statement
     */
    public static void main(String[] args) {
        ValuesStatement.Builder<String> builder = ValuesStatement.newBuilder(Values.protectedStrings());

        builder.tableName("example_table")
                .addLabel("example_column_label");

        for (String arg : args) {
            builder.addValue(arg);
        }

        logger.info("Input arguments as a values statement: {}", builder.build().getSQL());
    }

}
