```
package de.consorsbank.core.trauth.util;

import com.consorsbank.common.error.handling.exception.CommonException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapperUtilsTest {

    @Nested
    class ToExecutionData {

        @ParameterizedTest
        @NullAndEmptySource
        void givenNullOrEmptyBytes_whenToExecutionData_thenReturnsNull(byte[] data) {
            var result = MapperUtils.toExecutionData(data);

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "   ", "\n", "\t \n"})
        void givenBlankContent_whenToExecutionData_thenReturnsNull(String blank) {
            var result = MapperUtils.toExecutionData(bytes(blank));

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"{}", "null", " {} "})
        void givenEmptyObjectOrJsonNull_whenToExecutionData_thenReturnsNull(String json) {
            var result = MapperUtils.toExecutionData(bytes(json));

            assertThat(result).isNull();
        }

        @Test
        void givenValidJson_whenToExecutionData_thenReturnsMap() {
            var json = """
                    {"exemptionOrderNumber":"12421"}
                    """;

            var result = MapperUtils.toExecutionData(bytes(json));

            assertThat(result).containsExactly(Map.entry("exemptionOrderNumber", "12421"));
        }

        @Test
        void givenNumberAsString_whenToExecutionData_thenKeepsLeadingZeros() {
            var json = """
                    {"exemptionOrderNumber":"0090559903887138"}
                    """;

            var result = MapperUtils.toExecutionData(bytes(json));

            assertThat(result).containsEntry("exemptionOrderNumber", "0090559903887138");
        }

        @Test
        void givenNestedJson_whenToExecutionData_thenReturnsNestedStructures() {
            var json = """
                    {"order":{"number":"12421","amount":980},"tags":["a","b"],"active":true}
                    """;

            var result = MapperUtils.toExecutionData(bytes(json));

            assertThat(result)
                    .containsEntry("order", Map.of("number", "12421", "amount", 980))
                    .containsEntry("tags", List.of("a", "b"))
                    .containsEntry("active", true);
        }

        @Test
        void givenUtf8Content_whenToExecutionData_thenDecodesCorrectly() {
            var json = """
                    {"city":"München","street":"Straße"}
                    """;

            var result = MapperUtils.toExecutionData(bytes(json));

            assertThat(result)
                    .containsEntry("city", "München")
                    .containsEntry("street", "Straße");
        }

        @ParameterizedTest
        @ValueSource(strings = {"not-json", "{\"a\":", "{a:1}", "[1,2,3]", "\"text\""})
        void givenInvalidOrNonObjectJson_whenToExecutionData_thenThrowsCommonException(String json) {
            assertThatThrownBy(() -> MapperUtils.toExecutionData(bytes(json)))
                    .isInstanceOf(CommonException.class);
        }
    }

@Nested
    class ToJson {

        @Test
        void givenNullMap_whenToJson_thenReturnsNull() {
            var result = MapperUtils.toJson(null);

            assertThat(result).isNull();
        }

        @Test
        void givenEmptyMap_whenToJson_thenReturnsNull() {
            var result = MapperUtils.toJson(Map.of());

            assertThat(result).isNull();
        }

        @Test
        void givenSingleEntry_whenToJson_thenReturnsJsonObject() {
            var result = MapperUtils.toJson(Map.of("exemptionOrderNumber", "12421"));

            assertThat(result).isEqualTo("{\"exemptionOrderNumber\":\"12421\"}");
        }

        @Test
        void givenStringWithLeadingZeros_whenToJson_thenKeepsItAsString() {
            var result = MapperUtils.toJson(Map.of("exemptionOrderNumber", "0090559903887138"));

            assertThat(result).isEqualTo("{\"exemptionOrderNumber\":\"0090559903887138\"}");
        }

        @Test
        void givenOrderedEntriesOfDifferentTypes_whenToJson_thenSerializesEachType() {
            var data = new LinkedHashMap<String, Object>();
            data.put("number", "12421");
            data.put("amount", 980);
            data.put("active", true);
            data.put("tags", List.of("a", "b"));
            data.put("order", Map.of("year", 2026));

            var result = MapperUtils.toJson(data);

            assertThat(result).isEqualTo(
                    "{\"number\":\"12421\",\"amount\":980,\"active\":true,\"tags\":[\"a\",\"b\"],\"order\":{\"year\":2026}}");
        }

        @Test
        void givenNullValue_whenToJson_thenWritesJsonNull() {
            var data = new HashMap<String, Object>();
            data.put("exemptionOrderNumber", null);

            var result = MapperUtils.toJson(data);

            assertThat(result).isEqualTo("{\"exemptionOrderNumber\":null}");
        }

        @Test
        void givenUtf8Content_whenToJson_thenKeepsCharacters() {
            var result = MapperUtils.toJson(Map.of("city", "München"));

            assertThat(result).isEqualTo("{\"city\":\"München\"}");
        }

        @Test
        void givenMap_whenToJsonThenToExecutionData_thenRoundTripReturnsEqualMap() {
            Map<String, Object> original = Map.of(
                    "exemptionOrderNumber", "12421",
                    "amount", 980,
                    "tags", List.of("a", "b"));

            var json = MapperUtils.toJson(original);
            var result = MapperUtils.toExecutionData(bytes(json));

            assertThat(result).isEqualTo(original);
        }

        @Test
        void givenNonSerializableValue_whenToJson_thenThrowsJacksonException() {
            Map<String, Object> data = Map.of("value", new Object());

            assertThatThrownBy(() -> MapperUtils.toJson(data))
                    .isInstanceOf(JacksonException.class);
        }
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}

```
