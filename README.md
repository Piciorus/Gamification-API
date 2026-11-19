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

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}

```
