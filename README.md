
zgrep -i "SendTechnicalEmail" * | grep "<ns2:data>" | sed -n 's/.*<ns2:data>\(.*\)<\/ns2:data>.*/\1/p' | head -n 1
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Date;

public class EvidenceServiceTest {

    @Test
    void testOverlapAndSameType() {
        EvidenceElen oldEvi = new EvidenceElen("TypeA", "SubA", "2025-01-01", "2025-01-31");
        EvidenceElen newEvi = new EvidenceElen("TypeA", "SubA", "2025-01-15", "2025-02-10");

        EvidenceService service = new EvidenceService();
        assertTrue(service.isOldEvidenceAffected(newEvi, oldEvi));
    }

    @Test
    void testNoOverlapSameType() {
        EvidenceElen oldEvi = new EvidenceElen("TypeA", "SubA", "2025-01-01", "2025-01-31");
        EvidenceElen newEvi = new EvidenceElen("TypeA", "SubA", "2025-02-01", "2025-02-28");

        EvidenceService service = new EvidenceService();
        assertFalse(service.isOldEvidenceAffected(newEvi, oldEvi));
    }

    @Test
    void testDifferentTypeOverlap() {
        EvidenceElen oldEvi = new EvidenceElen("TypeA", "SubA", "2025-01-01", "2025-01-31");
        EvidenceElen newEvi = new EvidenceElen("TypeB", "SubA", "2025-01-15", "2025-02-10");

        EvidenceService service = new EvidenceService();
        assertFalse(service.isOldEvidenceAffected(newEvi, oldEvi));
    }

    @Test
    void testCheckEvidenceStartDateSetsTodayIfNull() {
        EvidenceElen evi = new EvidenceElen("TypeA", "SubA", null, "2025-01-31");

        EvidenceService service = new EvidenceService();
        service.checkEvidenceStartDate(evi);

        assertNotNull(evi.getEvidenceStartDate());
        assertEquals(service.getToday().toString(), evi.getEvidenceStartDate());
    }
}
