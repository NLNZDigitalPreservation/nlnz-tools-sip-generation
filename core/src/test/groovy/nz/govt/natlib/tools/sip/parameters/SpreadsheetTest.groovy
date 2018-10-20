package nz.govt.natlib.tools.sip.parameters

import nz.govt.natlib.tools.sip.generation.SipGenerationException
import nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet

import static org.hamcrest.core.Is.is
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.when

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

/**
 * Tests {@link nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet}.
 */
@RunWith(MockitoJUnitRunner.class)
class SpreadsheetTest {
    static Map<String, String> ROW_SAMPLE_ONE = [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static Map<String, String> ROW_SAMPLE_TWO = [ 'column-1': 'row2', 'column 2': 'another' ]
    static Map<String, String> ROW_SAMPLE_THREE = [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4' ]
    static Map<String, String> ROW_SAMPLE_DUPLICATE_ONE = [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static Map<String, String> ROW_SAMPLE_NO_COLUMN_1 = [ 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]

    @Test
    void rowsWithIdsAreValidSpreadsheet() {
        List<Map<String, String>> rows = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE ]
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, false)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has no rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() == 0)
        assertTrue("Spreadsheet has no rows without ids", spreadsheet.rowsWithoutIds().size() == 0)
    }

    @Test(expected = SipGenerationException.class)
    void rowsWithDuplicateIdsAreInvalidIfNotAllowed() {
        List<Map<String, String>> rows = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_DUPLICATE_ONE ]
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, false)

        assertTrue("Spreadsheet is invalid", !spreadsheet.isValid())
        assertTrue("Spreadsheet has rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() > 0)
    }

    @Test
    void rowsWithDuplicateIdsAreValidIfAllowed() {
        List<Map<String, String>> rows = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_DUPLICATE_ONE ]
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, true, false)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() > 0)
    }

    @Test(expected = SipGenerationException.class)
    void rowsWithoutIdsAreInvalidIfNotAllowed() {
        List<Map<String, String>> rows = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_NO_COLUMN_1 ]
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, false)

        assertTrue("Spreadsheet is invalid", !spreadsheet.isValid())
        assertTrue("Spreadsheet has rows without ids", spreadsheet.rowsWithoutIds().size() > 0)
    }

    @Test
    void rowsWithoutIdsAreValidIfAllowed() {
        List<Map<String, String>> rows = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_NO_COLUMN_1 ]
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, true)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has rows without ids", spreadsheet.rowsWithoutIds().size() > 0)
    }
}
