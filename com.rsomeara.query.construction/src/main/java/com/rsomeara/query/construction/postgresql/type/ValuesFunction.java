package com.rsomeara.query.construction.postgresql.type;

import java.util.List;
import java.util.function.Function;

/**
 * Converts a data representation to a set of SQL-consumable values.
 *
 * <p>
 * This is used instead of the general function interface for code readability - clients may wrap existing functions via
 * {@link Values#asValuesFunction(Function)}
 * </p>
 *
 * @author romeara
 *
 * @param <T>
 *            Type to represent as SQL-consumable data
 */
public interface ValuesFunction<T> extends Function<T, List<String>> {

}
