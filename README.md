```
package com.consors.common.util.onTopOfJava;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for DBUtils.
 */
public class DBUtilsTestCase {

    @Mock
    private Connection mockConnection;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // -------------------------------------------------------------------------
    // commitConnection tests
    // -------------------------------------------------------------------------

    @Test
    public void testCommitConnectionWithNullConnectionReturnsTrue() throws Exception {
        assertTrue(DBUtils.commitConnection(null));
    }

    @Test
    public void testCommitConnectionSuccessReturnsTrue() throws Exception {
        doNothing().when(mockConnection).commit();

        assertTrue(DBUtils.commitConnection(mockConnection));
        verify(mockConnection, times(1)).commit();
    }

    @Test
    public void testCommitConnectionSQLExceptionReturnsFalse() throws Exception {
        doThrow(new SQLException("commit failed")).when(mockConnection).commit();

        assertFalse(DBUtils.commitConnection(mockConnection));
        verify(mockConnection, times(1)).commit();
    }

    // -------------------------------------------------------------------------
    // rollbackConnection tests
    // -------------------------------------------------------------------------

    @Test
    public void testRollbackConnectionWithNullConnectionReturnsTrue() throws Exception {
        assertTrue(DBUtils.rollbackConnection(null));
    }

    @Test
    public void testRollbackConnectionSuccessReturnsTrue() throws Exception {
        doNothing().when(mockConnection).rollback();

        assertTrue(DBUtils.rollbackConnection(mockConnection));
        verify(mockConnection, times(1)).rollback();
    }

    @Test
    public void testRollbackConnectionSQLExceptionReturnsFalse() throws Exception {
        doThrow(new SQLException("rollback failed")).when(mockConnection).rollback();

        assertFalse(DBUtils.rollbackConnection(mockConnection));
        verify(mockConnection, times(1)).rollback();
    }
}

```
```
package com.consors.common.util.onTopOfJava;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for FileUtils.
 */
public class FileUtilsTestCase {

    // -------------------------------------------------------------------------
    // close(InputStream) tests
    // -------------------------------------------------------------------------

    @Test
    public void testCloseInputStreamWithNullDoesNotThrow() {
        // Should silently do nothing
        FileUtils.close((InputStream) null);
    }

    @Test
    public void testCloseInputStreamClosesSuccessfully() throws Exception {
        final InputStream mockInputStream = mock(InputStream.class);
        doNothing().when(mockInputStream).close();

        FileUtils.close(mockInputStream);

        verify(mockInputStream, times(1)).close();
    }

    @Test
    public void testCloseInputStreamLogsWarningOnIOException() throws Exception {
        final InputStream mockInputStream = mock(InputStream.class);
        doThrow(new IOException("close failed")).when(mockInputStream).close();

        // Should not throw — exception is caught and logged as warning
        FileUtils.close(mockInputStream);

        verify(mockInputStream, times(1)).close();
    }

    @Test
    public void testCloseRealInputStreamFromByteArray() throws Exception {
        final InputStream is = new ByteArrayInputStream("hello".getBytes());
        // Should close without exception
        FileUtils.close(is);
    }

    // -------------------------------------------------------------------------
    // close(OutputStream) tests
    // -------------------------------------------------------------------------

    @Test
    public void testCloseOutputStreamWithNullDoesNotThrow() {
        FileUtils.close((OutputStream) null);
    }

    @Test
    public void testCloseOutputStreamClosesSuccessfully() throws Exception {
        final OutputStream mockOutputStream = mock(OutputStream.class);
        doNothing().when(mockOutputStream).close();

        FileUtils.close(mockOutputStream);

        verify(mockOutputStream, times(1)).close();
    }

    @Test
    public void testCloseOutputStreamLogsWarningOnIOException() throws Exception {
        final OutputStream mockOutputStream = mock(OutputStream.class);
        doThrow(new IOException("close failed")).when(mockOutputStream).close();

        // Should not throw — exception is caught and logged as warning
        FileUtils.close(mockOutputStream);

        verify(mockOutputStream, times(1)).close();
    }

    @Test
    public void testCloseRealOutputStream() throws Exception {
        final OutputStream os = new ByteArrayOutputStream();
        FileUtils.close(os);
    }
}

```



```
package com.consors.common.util.onTopOfJava;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for MapUtil.
 */
public class MapUtilTestCase {

    // -------------------------------------------------------------------------
    // getKeysStartingWith tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetKeysStartingWithReturnsMatchingEntries() {
        final Map<String, Object> map = new HashMap<>();
        map.put("user.name", "Alice");
        map.put("user.age", 30);
        map.put("order.id", 99);

        final Map<String, Object> result = MapUtil.getKeysStartingWith(map, "user.");

        assertEquals(2, result.size());
        assertTrue(result.containsKey("user.name"));
        assertTrue(result.containsKey("user.age"));
        assertFalse(result.containsKey("order.id"));
    }

    @Test
    public void testGetKeysStartingWithNoMatchReturnsEmptyMap() {
        final Map<String, Object> map = new HashMap<>();
        map.put("foo.bar", "value");

        final Map<String, Object> result = MapUtil.getKeysStartingWith(map, "baz.");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetKeysStartingWithEmptyMapReturnsEmptyMap() {
        final Map<String, Object> map = new HashMap<>();

        final Map<String, Object> result = MapUtil.getKeysStartingWith(map, "prefix.");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetKeysStartingWithEmptyPrefixReturnsAllEntries() {
        final Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        final Map<String, Object> result = MapUtil.getKeysStartingWith(map, "");

        assertEquals(2, result.size());
    }

    @Test
    public void testGetKeysStartingWithPreservesValues() {
        final Map<String, Object> map = new HashMap<>();
        map.put("config.timeout", 5000);

        final Map<String, Object> result = MapUtil.getKeysStartingWith(map, "config.");

        assertEquals(5000, result.get("config.timeout"));
    }

    @Test
    public void testGetKeysStartingWithAllKeysMatch() {
        final Map<String, Object> map = new HashMap<>();
        map.put("db.host", "localhost");
        map.put("db.port", 5432);
        map.put("db.name", "mydb");

        final Map<String, Object> result = MapUtil.getKeysStartingWith(map, "db.");

        assertEquals(3, result.size());
    }
}

```


```
package com.consors.common.util.onTopOfJava;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for MWBigDecimal.
 */
public class MWBigDecimalTestCase {

    private MWBigDecimal value10;
    private MWBigDecimal value20;
    private MWBigDecimal value5;
    private MWBigDecimal valueNeg10;

    @Before
    public void setUp() {
        value10   = new MWBigDecimal("10");
        value20   = new MWBigDecimal("20");
        value5    = new MWBigDecimal("5");
        valueNeg10 = new MWBigDecimal("-10");
    }

    // -------------------------------------------------------------------------
    // toString tests
    // -------------------------------------------------------------------------

    @Test
    public void testToStringNoScale() {
        final MWBigDecimal val = new MWBigDecimal("3.14159");
        assertNotNull(val.toString());
        assertTrue(val.toString().startsWith("3.14"));
    }

    @Test
    public void testToStringWithScale2() {
        final MWBigDecimal val = new MWBigDecimal("3.14159");
        assertEquals("3.14", val.toString(2));
    }

    @Test
    public void testToStringWithScale0() {
        final MWBigDecimal val = new MWBigDecimal("3.7");
        assertEquals("4", val.toString(0));
    }

    // -------------------------------------------------------------------------
    // multiply tests
    // -------------------------------------------------------------------------

    @Test
    public void testMultiplyPositiveValues() {
        value10.multiply(value20);
        assertEquals(0, value10.compareTo(new MWBigDecimal("200")));
    }

    @Test
    public void testMultiplyByZero() {
        value10.multiply(new MWBigDecimal("0"));
        assertEquals(0, value10.compareTo(new MWBigDecimal("0")));
    }

    @Test
    public void testMultiplyByNegative() {
        value10.multiply(valueNeg10);
        assertEquals(0, value10.compareTo(new MWBigDecimal("-100")));
    }

    // -------------------------------------------------------------------------
    // add tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddPositiveValues() {
        value10.add(value5);
        assertEquals(0, value10.compareTo(new MWBigDecimal("15")));
    }

    @Test
    public void testAddNegativeValue() {
        value10.add(valueNeg10);
        assertEquals(0, value10.compareTo(new MWBigDecimal("0")));
    }

    // -------------------------------------------------------------------------
    // subtract tests
    // -------------------------------------------------------------------------

    @Test
    public void testSubtractPositiveValue() {
        value20.subtract(value5);
        assertEquals(0, value20.compareTo(new MWBigDecimal("15")));
    }

    @Test
    public void testSubtractResultingInNegative() {
        value5.subtract(value20);
        assertEquals(0, value5.compareTo(new MWBigDecimal("-15")));
    }

    // -------------------------------------------------------------------------
    // negate tests
    // -------------------------------------------------------------------------

    @Test
    public void testNegatePositive() {
        value10.negate();
        assertEquals(0, value10.compareTo(new MWBigDecimal("-10")));
    }

    @Test
    public void testNegateNegative() {
        valueNeg10.negate();
        assertEquals(0, valueNeg10.compareTo(new MWBigDecimal("10")));
    }

    @Test
    public void testNegateZero() {
        final MWBigDecimal zero = new MWBigDecimal("0");
        zero.negate();
        assertEquals(0, zero.compareTo(new MWBigDecimal("0")));
    }

    // -------------------------------------------------------------------------
    // abs tests
    // -------------------------------------------------------------------------

    @Test
    public void testAbsOfNegative() {
        valueNeg10.abs();
        assertEquals(0, valueNeg10.compareTo(new MWBigDecimal("10")));
    }

    @Test
    public void testAbsOfPositive() {
        value10.abs();
        assertEquals(0, value10.compareTo(new MWBigDecimal("10")));
    }

    // -------------------------------------------------------------------------
    // min tests
    // -------------------------------------------------------------------------

    @Test
    public void testMinReturnsSmaller() {
        value20.min(value5);
        assertEquals(0, value20.compareTo(new MWBigDecimal("5")));
    }

    @Test
    public void testMinWithEqualValues() {
        value10.min(new MWBigDecimal("10"));
        assertEquals(0, value10.compareTo(new MWBigDecimal("10")));
    }

    // -------------------------------------------------------------------------
    // max tests
    // -------------------------------------------------------------------------

    @Test
    public void testMaxReturnsLarger() {
        value5.max(value20);
        assertEquals(0, value5.compareTo(new MWBigDecimal("20")));
    }

    @Test
    public void testMaxWithEqualValues() {
        value10.max(new MWBigDecimal("10"));
        assertEquals(0, value10.compareTo(new MWBigDecimal("10")));
    }

    // -------------------------------------------------------------------------
    // divide tests
    // -------------------------------------------------------------------------

    @Test
    public void testDivideExact() {
        value20.divide(value5);
        assertEquals(0, value20.compareTo(new MWBigDecimal("4")));
    }

    @Test
    public void testDivideWithRemainder() {
        value10.divide(new MWBigDecimal("3"));
        // Result is rounded half-up to MAX_SCALE (15); just check it's close to 3.333...
        final MWBigDecimal expected = new MWBigDecimal("3.33333");
        assertTrue(value10.compareTo(expected) > 0); // more precise than 5 dp
    }

    // -------------------------------------------------------------------------
    // compareTo tests
    // -------------------------------------------------------------------------

    @Test
    public void testCompareToEqualValues() {
        assertEquals(0, new MWBigDecimal("2.0").compareTo(new MWBigDecimal("2.00")));
    }

    @Test
    public void testCompareToLessThan() {
        assertTrue(value5.compareTo(value10) < 0);
    }

    @Test
    public void testCompareToGreaterThan() {
        assertTrue(value20.compareTo(value10) > 0);
    }

    // -------------------------------------------------------------------------
    // getClone tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetCloneIsIndependent() {
        final MWBigDecimal original = new MWBigDecimal("42");
        final MWBigDecimal clone = original.getClone();

        clone.add(new MWBigDecimal("1"));

        assertEquals(0, original.compareTo(new MWBigDecimal("42")));
        assertEquals(0, clone.compareTo(new MWBigDecimal("43")));
    }

    @Test
    public void testGetCloneHasSameValue() {
        final MWBigDecimal clone = value10.getClone();
        assertEquals(0, value10.compareTo(clone));
    }
}

```


```
package com.consors.common.util.onTopOfJava;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Test class for Serializer.
 */
public class SerializerTestCase {

    // -------------------------------------------------------------------------
    // deserialize tests
    // -------------------------------------------------------------------------

    @Test
    public void testDeserializeValidByteArrayReturnsObject() throws Exception {
        final String original = "Hello, World!";
        final byte[] bytes = toByteArray(original);

        final Object result = Serializer.deserialize(bytes);

        assertNotNull(result);
        assertEquals(original, result);
    }

    @Test
    public void testDeserializeSerializableObjectRoundTrip() throws Exception {
        final Integer original = 12345;
        final byte[] bytes = toByteArray(original);

        final Object result = Serializer.deserialize(bytes);

        assertEquals(original, result);
    }

    @Test
    public void testDeserializeInvalidByteArrayReturnsNull() {
        final byte[] garbage = new byte[]{0x01, 0x02, 0x03};

        // Should not throw; bad data is caught internally and logs fatal
        final Object result = Serializer.deserialize(garbage);

        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // toReadable tests
    // -------------------------------------------------------------------------

    @Test
    public void testToReadableNullReturnsNull() {
        assertNull(Serializer.toReadable(null));
    }

    @Test
    public void testToReadableEmptyArrayReturnsEmptyString() {
        final String result = Serializer.toReadable(new byte[0]);
        // empty array → empty hex string
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void testToReadableKnownByteProducesHex() {
        // byte 0xFF → "ff"
        final String result = Serializer.toReadable(new byte[]{(byte) 0xFF});
        assertNotNull(result);
        assertEquals("ff", result);
    }

    @Test
    public void testToReadableMultipleBytes() {
        final byte[] input = new byte[]{(byte) 0x00, (byte) 0xAB, (byte) 0xCD};
        final String result = Serializer.toReadable(input);
        assertNotNull(result);
        assertEquals(6, result.length()); // 2 hex chars per byte
        assertEquals("00abcd", result);
    }

    // -------------------------------------------------------------------------
    // fromReadable tests
    // -------------------------------------------------------------------------

    @Test
    public void testFromReadableNullReturnsEmptyArray() {
        final byte[] result = Serializer.fromReadable(null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testFromReadableKnownHexProducesByte() {
        final byte[] result = Serializer.fromReadable("ff");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals((byte) 0xFF, result[0]);
    }

    @Test
    public void testFromReadableRoundTripWithToReadable() {
        final byte[] original = new byte[]{(byte) 0x10, (byte) 0x20, (byte) 0x30};
        final String readable = Serializer.toReadable(original);
        final byte[] restored = Serializer.fromReadable(readable);

        assertArrayEquals(original, restored);
    }

    @Test
    public void testFromReadableMultipleBytes() {
        final byte[] result = Serializer.fromReadable("00abcd");
        assertEquals(3, result.length);
        assertEquals((byte) 0x00, result[0]);
        assertEquals((byte) 0xAB, result[1]);
        assertEquals((byte) 0xCD, result[2]);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private byte[] toByteArray(final Serializable obj) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }
}

```


```
package com.consors.common.util.onTopOfJava;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for MessageFormat.
 */
public class MessageFormatTestCase {

    // -------------------------------------------------------------------------
    // replace(String, Map) tests
    // -------------------------------------------------------------------------

    @Test
    public void testReplaceSubstitutesKnownVariable() {
        final Map<String, Object> props = new HashMap<>();
        props.put("NAME", "Alice");

        final String result = MessageFormat.replace("Hello, ${NAME}!", props);

        assertEquals("Hello, Alice!", result);
    }

    @Test
    public void testReplaceUsesDefaultValueWhenKeyMissing() {
        final Map<String, Object> props = new HashMap<>();

        final String result = MessageFormat.replace("Hello, ${NAME:World}!", props);

        assertEquals("Hello, World!", result);
    }

    @Test
    public void testReplaceKeyOverridesDefaultValue() {
        final Map<String, Object> props = new HashMap<>();
        props.put("NAME", "Alice");

        final String result = MessageFormat.replace("Hello, ${NAME:World}!", props);

        assertEquals("Hello, Alice!", result);
    }

    @Test
    public void testReplaceMultipleVariables() {
        final Map<String, Object> props = new HashMap<>();
        props.put("FIRST", "John");
        props.put("LAST", "Doe");

        final String result = MessageFormat.replace("${FIRST} ${LAST}", props);

        assertEquals("John Doe", result);
    }

    @Test
    public void testReplaceNoVariablesReturnsOriginalString() {
        final Map<String, Object> props = new HashMap<>();

        final String result = MessageFormat.replace("No variables here.", props);

        assertEquals("No variables here.", result);
    }

    @Test
    public void testReplaceEmptyTemplateReturnsEmpty() {
        final Map<String, Object> props = new HashMap<>();

        final String result = MessageFormat.replace("", props);

        assertEquals("", result);
    }

    @Test
    public void testReplaceVariableWithNullMapValueUsesDefault() {
        final Map<String, Object> props = new HashMap<>();
        props.put("NAME", null);

        final String result = MessageFormat.replace("Hello, ${NAME:DefaultName}!", props);

        assertEquals("Hello, DefaultName!", result);
    }

    @Test
    public void testReplaceVariableWithNoDefaultAndMissingKeyProducesEmpty() {
        final Map<String, Object> props = new HashMap<>();

        final String result = MessageFormat.replace("Hello, ${MISSING}!", props);

        assertEquals("Hello, !", result);
    }

    @Test
    public void testReplaceAllOccurrencesOfSameVariable() {
        final Map<String, Object> props = new HashMap<>();
        props.put("X", "42");

        final String result = MessageFormat.replace("${X} and ${X}", props);

        assertEquals("42 and 42", result);
    }
}

```


```
package com.consors.common.util.onTopOfJava;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test class for Strings.
 */
public class StringsTestCase {

    // -------------------------------------------------------------------------
    // replace(String, Map) tests  — variable substitution with ${N:DEFAULT|length}
    // -------------------------------------------------------------------------

    @Test
    public void testReplaceSubstitutesVariable() {
        final Map<String, Object> props = new HashMap<>();
        props.put("CITY", "Berlin");

        final String result = Strings.replace("Welcome to ${CITY}!", props);

        assertEquals("Welcome to Berlin!", result);
    }

    @Test
    public void testReplaceUsesDefaultWhenKeyAbsent() {
        final Map<String, Object> props = new HashMap<>();

        final String result = Strings.replace("Hello ${NAME:stranger}!", props);

        assertEquals("Hello stranger!", result);
    }

    @Test
    public void testReplaceKeyOverridesDefault() {
        final Map<String, Object> props = new HashMap<>();
        props.put("NAME", "Alice");

        final String result = Strings.replace("Hello ${NAME:stranger}!", props);

        assertEquals("Hello Alice!", result);
    }

    @Test
    public void testReplaceMultipleVariables() {
        final Map<String, Object> props = new HashMap<>();
        props.put("A", "foo");
        props.put("B", "bar");

        final String result = Strings.replace("${A}-${B}", props);

        assertEquals("foo-bar", result);
    }

    @Test
    public void testReplaceLengthConstraintTruncatesValue() {
        final Map<String, Object> props = new HashMap<>();
        props.put("CODE", "ABCDEF");

        // |3 means max length 3
        final String result = Strings.replace("Code: ${CODE|3}", props);

        assertEquals("Code: ABC", result);
    }

    @Test
    public void testReplaceLengthConstraintNoTruncationWhenValueShorter() {
        final Map<String, Object> props = new HashMap<>();
        props.put("CODE", "AB");

        final String result = Strings.replace("Code: ${CODE|5}", props);

        assertEquals("Code: AB", result);
    }

    @Test
    public void testReplaceNoVariablesReturnsOriginal() {
        final Map<String, Object> props = new HashMap<>();

        final String result = Strings.replace("static text", props);

        assertEquals("static text", result);
    }

    @Test
    public void testReplaceEmptyStringReturnsEmpty() {
        final String result = Strings.replace("", new HashMap<>());

        assertEquals("", result);
    }

    @Test
    public void testReplaceNullValueInMapUsesDefault() {
        final Map<String, Object> props = new HashMap<>();
        props.put("KEY", null);

        final String result = Strings.replace("${KEY:fallback}", props);

        assertEquals("fallback", result);
    }

    @Test
    public void testReplaceIntegerValueConvertedToString() {
        final Map<String, Object> props = new HashMap<>();
        props.put("NUM", 99);

        final String result = Strings.replace("Number: ${NUM}", props);

        assertEquals("Number: 99", result);
    }

    // -------------------------------------------------------------------------
    // simpleMessageFormat tests
    // -------------------------------------------------------------------------

    @Test
    public void testSimpleMessageFormatSinglePlaceholder() {
        final String result = Strings.simpleMessageFormat("Hello {0}!", new Object[]{"World"});

        assertEquals("Hello World!", result);
    }

    @Test
    public void testSimpleMessageFormatMultiplePlaceholders() {
        final String result = Strings.simpleMessageFormat("{0} + {1} = {2}", new Object[]{1, 2, 3});

        assertEquals("1 + 2 = 3", result);
    }

    @Test
    public void testSimpleMessageFormatNoPlaceholders() {
        final String result = Strings.simpleMessageFormat("no placeholders", new Object[]{});

        assertEquals("no placeholders", result);
    }

    @Test
    public void testSimpleMessageFormatRepeatedPlaceholder() {
        final String result = Strings.simpleMessageFormat("{0} and {0}", new Object[]{"again"});

        assertEquals("again and again", result);
    }

    @Test
    public void testSimpleMessageFormatNullArgConvertedToString() {
        // args[i].toString() would throw NPE on null; behaviour depends on implementation.
        // Verifying it handles non-null args safely:
        final String result = Strings.simpleMessageFormat("value: {0}", new Object[]{"test"});

        assertEquals("value: test", result);
    }
}

```


```
package com.consors.common.util.onTopOfJava;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Test class for Time.
 */
public class TimeTestCase {

    private static final long BASE_MILLIS;

    static {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        BASE_MILLIS = cal.getTimeInMillis();
    }

    private Time time10h30;
    private Time time12h00;
    private Time time08h00;

    @Before
    public void setUp() {
        time10h30 = makeTime(10, 30, 0);
        time12h00 = makeTime(12, 0, 0);
        time08h00 = makeTime(8, 0, 0);
    }

    // -------------------------------------------------------------------------
    // after(Calendar) tests
    // -------------------------------------------------------------------------

    @Test
    public void testAfterReturnsTrueWhenTimeIsAfterGivenCalendar() {
        final Calendar earlier = calendarAt(9, 0, 0);
        assertTrue(time10h30.after(earlier));
    }

    @Test
    public void testAfterReturnsFalseWhenTimeIsBeforeGivenCalendar() {
        final Calendar later = calendarAt(11, 0, 0);
        assertFalse(time10h30.after(later));
    }

    @Test
    public void testAfterReturnsFalseWhenTimesAreEqual() {
        final Calendar same = calendarAt(10, 30, 0);
        assertFalse(time10h30.after(same));
    }

    // -------------------------------------------------------------------------
    // add(int field, int amount) tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddMinutesIncreasesTime() {
        time10h30.add(Calendar.MINUTE, 30);
        assertEquals(11, time10h30.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, time10h30.get(Calendar.MINUTE));
    }

    @Test
    public void testAddSecondsIncreasesTime() {
        time10h30.add(Calendar.SECOND, 90);
        assertEquals(31, time10h30.get(Calendar.MINUTE));
        assertEquals(30, time10h30.get(Calendar.SECOND));
    }

    @Test
    public void testAddNegativeAmountDecreasesTime() {
        time10h30.add(Calendar.MINUTE, -30);
        assertEquals(10, time10h30.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, time10h30.get(Calendar.MINUTE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddInvalidFieldThrowsIllegalArgumentException() {
        // DATE / YEAR / MONTH are invalid fields for Time
        time10h30.add(Calendar.DATE, 1);
    }

    // -------------------------------------------------------------------------
    // get(int field) tests
    // -------------------------------------------------------------------------

    @Test
    public void testGetHourOfDay() {
        assertEquals(10, time10h30.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testGetMinute() {
        assertEquals(30, time10h30.get(Calendar.MINUTE));
    }

    @Test
    public void testGetSecond() {
        assertEquals(0, time10h30.get(Calendar.SECOND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInvalidFieldThrowsIllegalArgumentException() {
        time10h30.get(Calendar.YEAR);
    }

    // -------------------------------------------------------------------------
    // clone() tests
    // -------------------------------------------------------------------------

    @Test
    public void testCloneReturnsDifferentInstance() {
        final Object cloned = time10h30.clone();
        assertNotSame(time10h30, cloned);
    }

    @Test
    public void testCloneHasSameTimeValue() {
        final Time cloned = (Time) time10h30.clone();
        assertEquals(time10h30.get(Calendar.HOUR_OF_DAY), cloned.get(Calendar.HOUR_OF_DAY));
        assertEquals(time10h30.get(Calendar.MINUTE), cloned.get(Calendar.MINUTE));
    }

    @Test
    public void testCloneIsIndependentOfOriginal() {
        final Time cloned = (Time) time10h30.clone();
        cloned.add(Calendar.MINUTE, 60);

        assertEquals(10, time10h30.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, time10h30.get(Calendar.MINUTE));

        assertEquals(11, cloned.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cloned.get(Calendar.MINUTE));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Time makeTime(final int hour, final int minute, final int second) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return new Time(cal.getTimeInMillis());
    }

    private Calendar calendarAt(final int hour, final int minute, final int second) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
}
```
