
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
}import org.junit.jupiter.api.Test;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrsEvidencesServiceTest {

    private final CrsEvidencesService service = new CrsEvidencesService();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    void testConvertStringToDate_validDate() throws Exception {
        String dateStr = "2025-08-14";
        Date date = service.convertStringToDate(dateStr);
        assertNotNull(date);
        assertEquals(dateStr, sdf.format(date));
    }

    @Test
    void testConvertStringToDate_invalidDate() {
        assertNull(service.convertStringToDate("invalid-date"));
    }

    @Test
    void testCheckEvidenceStartDate_setsDateIfNull() {
        EvidenceElem evidence = mock(EvidenceElem.class);

        when(evidence.getEvidenceStartDate()).thenReturn(null);

        service.checkEvidenceStartDate(evidence);

        verify(evidence).setEvidenceStartDate(anyString());
    }

    @Test
    void testCheckEvidenceStartDate_doesNothingIfDatePresent() {
        EvidenceElem evidence = mock(EvidenceElem.class);

        when(evidence.getEvidenceStartDate()).thenReturn("2025-08-14");

        service.checkEvidenceStartDate(evidence);

        verify(evidence, never()).setEvidenceStartDate(anyString());
    }

    @Test
    void testIsOldEvidenceAffected_sameTypeAndOverlap() {
        EvidenceElem newEvidence = mock(EvidenceElem.class);
        EvidenceElem oldEvidence = mock(EvidenceElem.class);

        when(newEvidence.getEvidenceStartDate()).thenReturn("2025-08-10");
        when(newEvidence.getEvidenceEndDate()).thenReturn("2025-08-20");
        when(oldEvidence.getEvidenceStartDate()).thenReturn("2025-08-15");
        when(oldEvidence.getEvidenceEndDate()).thenReturn("2025-08-25");

        when(newEvidence.getEvidenceType()).thenReturn("TypeA");
        when(newEvidence.getEvidenceSubType()).thenReturn("Sub1");
        when(oldEvidence.getEvidenceType()).thenReturn("TypeA");
        when(oldEvidence.getEvidenceSubType()).thenReturn("Sub1");

        boolean result = service.isOldEvidenceAffected(newEvidence, oldEvidence);

        assertTrue(result);
    }

    @Test
    void testIsOldEvidenceAffected_differentType() {
        EvidenceElem newEvidence = mock(EvidenceElem.class);
        EvidenceElem oldEvidence = mock(EvidenceElem.class);

        when(newEvidence.getEvidenceStartDate()).thenReturn("2025-08-10");
        when(newEvidence.getEvidenceEndDate()).thenReturn("2025-08-20");
        when(oldEvidence.getEvidenceStartDate()).thenReturn("2025-08-15");
        when(oldEvidence.getEvidenceEndDate()).thenReturn("2025-08-25");

        when(newEvidence.getEvidenceType()).thenReturn("TypeA");
        when(newEvidence.getEvidenceSubType()).thenReturn("Sub1");
        when(oldEvidence.getEvidenceType()).thenReturn("TypeB");
        when(oldEvidence.getEvidenceSubType()).thenReturn("Sub1");

        boolean result = service.isOldEvidenceAffected(newEvidence, oldEvidence);

        assertFalse(result);
    }
}
da
import org.junit.jupiter.api.Test;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrsEvidencesServiceTest {

    private final CrsEvidencesService service = new CrsEvidencesService();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    void testConvertStringToDate_validDate() throws Exception {
        String dateStr = "2025-08-14";
        Date date = service.convertStringToDate(dateStr);
        assertNotNull(date);
        assertEquals(dateStr, sdf.format(date));
    }

    @Test
    void testConvertStringToDate_invalidDate() {
        assertNull(service.convertStringToDate("invalid-date"));
    }

    @Test
    void testConvertStringToDate_nullDate() {
        assertNull(service.convertStringToDate(null));
    }

    @Test
    void testCheckEvidenceStartDate_setsDateIfNull() {
        EvidenceElem evidence = mock(EvidenceElem.class);
        when(evidence.getEvidenceStartDate()).thenReturn(null);
        service.checkEvidenceStartDate(evidence);
        verify(evidence).setEvidenceStartDate(anyString());
    }

    @Test
    void testCheckEvidenceStartDate_doesNothingIfDatePresent() {
        EvidenceElem evidence = mock(EvidenceElem.class);
        when(evidence.getEvidenceStartDate()).thenReturn("2025-08-14");
        service.checkEvidenceStartDate(evidence);
        verify(evidence, never()).setEvidenceStartDate(anyString());
    }

    @Test
    void testIsOldEvidenceAffected_sameTypeAndOverlap() {
        EvidenceElem newEvidence = mockEvidence("TypeA", "Sub1", "2025-08-10", "2025-08-20");
        EvidenceElem oldEvidence = mockEvidence("TypeA", "Sub1", "2025-08-15", "2025-08-25");
        assertTrue(service.isOldEvidenceAffected(newEvidence, oldEvidence));
    }

    @Test
    void testIsOldEvidenceAffected_differentType() {
        EvidenceElem newEvidence = mockEvidence("TypeA", "Sub1", "2025-08-10", "2025-08-20");
        EvidenceElem oldEvidence = mockEvidence("TypeB", "Sub1", "2025-08-15", "2025-08-25");
        assertFalse(service.isOldEvidenceAffected(newEvidence, oldEvidence));
    }

    @Test
    void testIsOldEvidenceAffected_nullDates() {
        EvidenceElem newEvidence = mockEvidence("TypeA", "Sub1", null, "2025-08-20");
        EvidenceElem oldEvidence = mockEvidence("TypeA", "Sub1", "2025-08-15", "2025-08-25");
        assertFalse(service.isOldEvidenceAffected(newEvidence, oldEvidence));
    }

    @Test
    void testIsOldEvidenceAffected_noOverlap() {
        EvidenceElem newEvidence = mockEvidence("TypeA", "Sub1", "2025-09-01", "2025-09-10");
        EvidenceElem oldEvidence = mockEvidence("TypeA", "Sub1", "2025-08-01", "2025-08-05");
        assertFalse(service.isOldEvidenceAffected(newEvidence, oldEvidence));
    }

    private EvidenceElem mockEvidence(String type, String subType, String startDate, String endDate) {
        EvidenceElem evidence = mock(EvidenceElem.class);
        when(evidence.getEvidenceType()).thenReturn(type);
        when(evidence.getEvidenceSubType()).thenReturn(subType);
        when(evidence.getEvidenceStartDate()).thenReturn(startDate);
        when(evidence.getEvidenceEndDate()).thenReturn(endDate);
        return evidence;
    }
}


