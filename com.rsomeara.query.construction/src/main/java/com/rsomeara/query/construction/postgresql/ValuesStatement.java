package com.rsomeara.query.construction.postgresql;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.rsomeara.query.construction.postgresql.type.ValuesFunction;

/**
 * Represents a PostgreSQL construct called a "values statement" which may be joined to a query in the same way as a
 * table, allowing insertion of static data lookups into query results
 *
 * @author romeara
 * @since 0.1
 */
@Immutable
public final class ValuesStatement {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(ValuesStatement.class);

    private final String statement;

    /**
     * @param builder
     *            Statement builder which contains information required to generate a values statement
     */
    private ValuesStatement(@Nonnull Builder<?> builder) {
        Objects.requireNonNull(builder);
        Objects.requireNonNull(builder.values);
        Objects.requireNonNull(builder.asTableName);
        Objects.requireNonNull(builder.labels);

        statement = new StringBuilder().append(buildValuesStatement(builder.values))
                .append(' ').append(builder.asTableName)
                .append(buildLabelsStatement(builder.labels)).toString();
    }

    /**
     * @return The values represented as a SQL statement, which can be joined to PostgreSQL queries
     * @since 0.1
     */
    @Nonnull
    public String getSQL() {
        return statement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(statement);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (this == obj) {
            result = true;
        } else if (obj instanceof ValuesStatement) {
            ValuesStatement compare = (ValuesStatement) obj;

            result = Objects.equals(compare.getSQL(), getSQL());
        }

        return result;
    }

    @Override
    public String toString() {
        return statement;
    }

    /**
     * @param valuesFunction
     *            Function which converts between a data representation and SQL-consumable representation
     * @return A new, empty builder for incremental value statement construction
     * @since 0.1
     */
    @Nonnull
    public static <T> Builder<T> newBuilder(@Nonnull ValuesFunction<T> valuesFunction) {
        Objects.requireNonNull(valuesFunction);

        return new Builder<T>(valuesFunction);
    }

    /**
     * @param values
     *            Groups of data to represent as a values statement
     * @return SQL snippet which represents the provided data in a way which can be joined to by PostgreSQL queries
     */
    @Nonnull
    private static String buildValuesStatement(@Nonnull Iterable<List<String>> values) {
        Objects.requireNonNull(values);

        StringBuilder valuesBuilder = new StringBuilder();

        List<String> valueRows = Lists.newLinkedList();

        // For each row of values, separate data with a comma and contain within a set of parentheses. Note that no
        // escaping of special characters is done - this processing is expected to be handled by the client calling
        // this function before it is invoked
        for (List<String> valueRow : values) {
            String row = new StringBuilder().append('(').append(Joiner.on(',').skipNulls().join(valueRow)).append(')').toString();
            valueRows.add(row);
            logger.trace("Built value row {}", row);
        }

        // Combine all values rows with a comma, and contain within a "values" group
        valuesBuilder.append("(values ").append(Joiner.on(',').skipNulls().join(valueRows)).append(")");

        return valuesBuilder.toString();
    }

    /**
     * @param valueLabels
     *            Group of labels to create a SQL snippet representation for
     * @return A SQL snippet which, when inserted into a values statement, provides labels for data columns within the
     *         statement
     */
    @Nonnull
    private static String buildLabelsStatement(@Nonnull List<String> valueLabels) {
        Objects.requireNonNull(valueLabels);

        // Create a parentheses-contained label statement
        return new StringBuilder().append('(').append(Joiner.on(',').skipNulls().join(valueLabels)).append(')').toString();
    }

    /**
     * Allows incremental construction of immutable values statements
     *
     * @author romeara
     *
     * @param <T>
     *            Type which represents the data to provide as data in a values statement
     * @since 0.1
     */
    public static final class Builder<T> {

        private final ValuesFunction<T> valuesFunction;

        private List<List<String>> values;

        private List<String> labels;

        private String asTableName;

        /**
         * @param valuesFunction
         *            Function which converts an arbitrary data representation into a set of SQL-consumable data
         */
        private Builder(@Nonnull ValuesFunction<T> valuesFunction) {
            Objects.requireNonNull(valuesFunction);

            this.valuesFunction = valuesFunction;
            values = Lists.newLinkedList();
            labels = Lists.newLinkedList();
            asTableName = null;
        }

        /**
         * @param tableName
         *            The handle to assign to the generated values statement - used when referencing data within a query
         * @return This builder instance
         * @since 0.1
         */
        @Nonnull
        public Builder<T> tableName(@Nonnull String tableName) {
            Objects.requireNonNull(tableName);

            this.asTableName = tableName;

            return this;
        }

        /**
         * @param value
         *            Value to add to the values provided by the statement
         * @return This builder instance
         * @since 0.1
         */
        @Nonnull
        public Builder<T> addValue(@Nonnull T value) {
            Objects.requireNonNull(value);

            values.add(valuesFunction.apply(value));

            return this;
        }

        /**
         * @param values
         *            Group of values to add to the values provided by the statement
         * @return This builder instance
         * @since 0.1
         */
        @Nonnull
        public Builder<T> addValues(@Nonnull Iterable<T> values) {
            Objects.requireNonNull(values);

            for (T value : values) {
                addValue(value);
            }

            return this;
        }

        /**
         * @param label
         *            Adds a label to the set of labels for data columns in the values statement
         * @return This builder instance
         * @since 0.1
         */
        @Nonnull
        public Builder<T> addLabel(@Nonnull String label) {
            Objects.requireNonNull(label);

            this.labels.add(label);

            return this;
        }

        /**
         * @param labels
         *            Group of labels to add (in the provided order) to the set of labels for data columns in the values
         *            statement
         * @return This builder instance
         * @since 0.1
         */
        @Nonnull
        public Builder<T> addLabels(@Nonnull List<String> labels) {
            Objects.requireNonNull(labels);

            // We could use addAll here, but individual means we ensure the non-null condition is met for each label in
            // the list
            for (String label : labels) {
                addLabel(label);
            }

            return this;
        }

        /**
         * @return An immutable ValuesStatement instance which represents the provided values
         * @since 0.1
         */
        @Nonnull
        public ValuesStatement build() {
            Preconditions.checkArgument(asTableName != null, "Values statements must have a handle to be referenced by query statements");
            Preconditions.checkArgument(!labels.isEmpty(), "Labels are required to reference value data");
            Preconditions.checkArgument(!values.isEmpty(), "Must have at least one value row to construct a values statement");

            return new ValuesStatement(this);
        }
    }
}
