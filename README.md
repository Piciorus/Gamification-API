import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSVUtils {

    /**
     * Reads CSV data from an InputStream and maps it to a list of entities.
     *
     * @param inputStream The InputStream containing the CSV data.
     * @param clazz The class of the entity to map the CSV data to.
     * @param <T> The type of the entity.
     * @return A list of mapped entities.
     * @throws IOException If an I/O error occurs.
     */
    public static <T> List<T> readCsv(InputStream inputStream, Class<T> clazz) throws IOException {
        List<T> entities = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            Map<String, Integer> headers = csvParser.getHeaderMap();

            for (CSVRecord csvRecord : csvParser) {
                T entity;
                try {
                    entity = clazz.getDeclaredConstructor().newInstance();

                    for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                        String header = entry.getKey();
                        String value = csvRecord.get(header);

                        // Use reflection to set the field value
                        Field field = clazz.getDeclaredField(header);
                        field.setAccessible(true);
                        // You might need to handle different field types
                        if (field.getType() == String.class) {
                            field.set(entity, value);
                        } else if (field.getType() == Integer.class || field.getType() == int.class) {
                            field.set(entity, Integer.parseInt(value));
                        } else if (field.getType() == BigDecimal.class) {
                            field.set(entity, new BigDecimal(value));
                        }
                        // Add more type conversions as needed
                    }

                    entities.add(entity);

                } catch (Exception e) {
                    throw new IOException("Error creating or setting entity: " + e.getMessage(), e);
                }
            }
        }
        return entities;
    }
}
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.persistence.Column;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVUtils {

    /**
     * Reads CSV data from an InputStream and maps it to a list of entities.
     *
     * @param inputStream The InputStream containing the CSV data.
     * @param clazz The class of the entity to map the CSV data to.
     * @param <T> The type of the entity.
     * @return A list of mapped entities.
     * @throws IOException If an I/O error occurs.
     */
    public static <T> List<T> readCsv(InputStream inputStream, Class<T> clazz) throws IOException {
        List<T> entities = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

            Map<String, String> headerToColumnMap = getHeaderToColumnMap(clazz);
            Map<String, Integer> csvHeaders = csvParser.getHeaderMap();

            for (CSVRecord csvRecord : csvParser) {
                T entity;
                try {
                    entity = clazz.getDeclaredConstructor().newInstance();

                    for (Map.Entry<String, Integer> csvHeader : csvHeaders.entrySet()) {
                        String csvHeaderName = csvHeader.getKey();
                        String columnName = headerToColumnMap.get(csvHeaderName);

                        if (columnName != null) {
                            String value = csvRecord.get(csvHeaderName);

                            Field field = getFieldByColumnName(clazz, columnName);
                            if (field != null) {
                                field.setAccessible(true);
                                // Set the field value based on the field type
                                if (field.getType() == String.class) {
                                    field.set(entity, value);
                                } else if (field.getType() == Integer.class || field.getType() == int.class) {
                                    field.set(entity, Integer.parseInt(value));
                                } else if (field.getType() == BigDecimal.class) {
                                    field.set(entity, new BigDecimal(value));
                                }
                                // Handle other data types as needed
                            }
                        }
                    }

                    entities.add(entity);

                } catch (Exception e) {
                    throw new IOException("Error creating or setting entity: " + e.getMessage(), e);
                }
            }
        }
        return entities;
    }

    /**
     * Creates a map of CSV header names to column names.
     *
     * @param clazz The entity class.
     * @return A map of CSV header names to column names.
     */
    private static <T> Map<String, String> getHeaderToColumnMap(Class<T> clazz) {
        Map<String, String> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                map.put(column.name(), field.getName());
            }
        }
        return map;
    }

    /**
     * Retrieves a field by its column name.
     *
     * @param clazz The entity class.
     * @param columnName The column name.
     * @return The field corresponding to the column name, or null if not found.
     */
    private static <T> Field getFieldByColumnName(Class<T> clazz, String columnName) {
        for (Field field : clazz.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.name().equals(columnName)) {
                return field;
            }
        }
        return null;
    }
}
import org.apache.commons.csv.CSVRecord;

@FunctionalInterface
public interface CsvRowMapper<T> {
    T mapRow(CSVRecord record);
}import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvUtils {

    public static <T> List<T> readCsv(InputStream inputStream, CsvRowMapper<T> rowMapper, String[] requiredHeaders) throws IOException {
        List<T> entities = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            
            Map<String, Integer> headers = parser.getHeaderMap();

            // Validate headers
            for (String requiredHeader : requiredHeaders) {
                if (!headers.containsKey(requiredHeader)) {
                    throw new IllegalArgumentException("CSV file is missing required header: " + requiredHeader);
                }
            }

            // Map each record to the entity
            for (CSVRecord record : parser.getRecords()) {
                entities.add(rowMapper.mapRow(record));
            }
        }

        return entities;
    }
}
import org.apache.commons.csv.CSVRecord;
import java.math.BigDecimal;

public class LutSegmentScoresEntity {
    private BigDecimal scoreId;
    private String segmentationId;
    private String scoreName;

    // Getters and setters...

    public static LutSegmentScoresEntity fromCsvRecord(CSVRecord record) {
        LutSegmentScoresEntity entity = new LutSegmentScoresEntity();
        entity.setScoreId(new BigDecimal(record.get("SCORE 10")));
        entity.setSegmentationId(record.get("SEGMENTATION ID"));
        entity.setScoreName(record.get("SCORE NAME"));
        return entity;
    }
}
CsvUtils {

    /**
     * Reads a CSV file from the given {@link InputStream}, validates the headers, and maps each row to an entity
     * using the provided {@link CsvRowMapper}.
     *
     * @param inputStream    The input stream of the CSV file.
     * @param rowMapper      A {@link CsvRowMapper} implementation that defines how to map CSV records to entities.
     * @param requiredHeaders An array of header names that are required to be present in the CSV file.
     * @param <T>            The type of the entity to be created from the CSV records.
     * @return A list of entities created from the CSV records.
     * @throws IOException If an I/O error occurs while reading the CSV file or parsing the records.
     * @throws IllegalArgumentException If any of the required headers are missing in the CSV file.
     */
