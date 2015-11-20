package nom.romeara.query.construction.test.postgresql;

import org.testng.Assert;
import org.testng.annotations.Test;

import nom.romeara.query.construction.postgresql.ValuesStatement;
import nom.romeara.query.construction.postgresql.type.Values;

/**
 * Tests general operation of construction of values statements
 *
 * @author romeara
 */
public class ValuesStatementTest {

    /**
     * @since 0.1
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void newBuilderNullFunction() throws Exception {
        ValuesStatement.newBuilder(null);
    }

    /**
     * @since 0.1
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void buildNoTableName() throws Exception {
        ValuesStatement.newBuilder(Values.protectedStrings()).addValue("value").addLabel("label").build();
    }

    /**
     * @since 0.1
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void buildNoValues() throws Exception {
        ValuesStatement.newBuilder(Values.protectedStrings()).tableName("table").addLabel("label").build();
    }

    /**
     * @since 0.1
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void buildNoLabels() throws Exception {
        ValuesStatement.newBuilder(Values.protectedStrings()).addValue("value").tableName("table").build();
    }

    /**
     * @since 0.1
     */
    @Test
    public void buildStatement() throws Exception {
        ValuesStatement statement = ValuesStatement.newBuilder(Values.protectedStrings())
                .tableName("table")
                .addValue("value1")
                .addValue("value2")
                .addLabel("label").build();

        Assert.assertEquals(statement.getSQL(), "(values (\'value1\'),(\'value2\')) table(label)");
    }

}
