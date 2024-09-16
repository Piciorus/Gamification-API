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
