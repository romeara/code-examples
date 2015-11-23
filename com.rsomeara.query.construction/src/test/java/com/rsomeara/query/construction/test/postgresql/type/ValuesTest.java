package com.rsomeara.query.construction.test.postgresql.type;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.rsomeara.query.construction.postgresql.type.Values;

/**
 * Tests general functionality of utility methods for value manipulation
 *
 * @author romeara
 */
public class ValuesTest {

    /**
     * @since.01
     */
    @Test
    public void protectStrings() throws Exception {
        Assert.assertEquals(Values.protectString().apply("string"), "\'string\'");
    }

    /**
     * @since.01
     */
    @Test
    public void escapeCharacters() throws Exception {
        Assert.assertEquals(Values.protectString().apply("str\'ing"), "\'str\'\'ing\'");
    }

    /**
     * @since.01
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void asValuesFunctionNullInput() throws Exception {
        Values.asValuesFunction(null);
    }

    /**
     * @since.01
     */
    @Test
    public void protectedStrings() throws Exception {
        Assert.assertEquals(Values.protectedStrings().apply("string"), Arrays.asList("\'string\'"));
    }

}
