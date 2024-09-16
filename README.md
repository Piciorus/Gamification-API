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