package nz.govt.natlib.tools.sip.parameters

import groovy.json.JsonOutput
import nz.govt.natlib.tools.sip.SipProcessingException
import nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse

import org.junit.Test

/**
 * Tests {@link nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet}.
 */
class SpreadsheetTest {
    static Map<String, String> ROW_SAMPLE_ONE = [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static Map<String, String> ROW_SAMPLE_TWO = [ 'column-1': 'row2', 'column 2': 'another' ]
    static Map<String, String> ROW_SAMPLE_THREE = [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4' ]
    static Map<String, String> ROW_SAMPLE_DUPLICATE_ONE = [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static Map<String, String> ROW_SAMPLE_NO_COLUMN_1 = [ 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]

    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_ALL_IDS_1 = [ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE ]
    static List<Map<String, String>> ROWS_DUPLICATE_ROWS_ALL_IDS_1 = [ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_DUPLICATE_ONE ]
    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1 = [ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_NO_COLUMN_1 ]

    @Test
    void rowsWithIdsAreValidSpreadsheet() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, false)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has no rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() == 0)
        assertTrue("Spreadsheet has no rows without ids", spreadsheet.rowsWithoutIds().size() == 0)
    }

    @Test(expected = SipProcessingException.class)
    void rowsWithDuplicateIdsAreInvalidIfNotAllowed() {
        List<Map<String, String>> rows = ROWS_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, false)

        assertTrue("Spreadsheet is invalid", !spreadsheet.isValid())
        assertTrue("Spreadsheet has rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() > 0)
    }

    @Test
    void rowsWithDuplicateIdsAreValidIfAllowed() {
        List<Map<String, String>> rows = ROWS_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, true, false)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() > 0)
    }

    @Test(expected = SipProcessingException.class)
    void rowsWithoutIdsAreInvalidIfNotAllowed() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, false)

        assertTrue("Spreadsheet is invalid", !spreadsheet.isValid())
        assertTrue("Spreadsheet has rows without ids", spreadsheet.rowsWithoutIds().size() > 0)
    }

    @Test
    void rowsWithoutIdsAreValidIfAllowed() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, true)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has rows without ids", spreadsheet.rowsWithoutIds().size() > 0)
    }

    @Test
    void importRowsNoDuplicatesAllWithIdsIsValidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-from-json-no-duplicate-rows-all-ids-1.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, false, false)
        assertTrue("Spreadsheet is valid", spreadsheet.isValid(false, false))

        Spreadsheet testSpreadsheetFromJsonAllIdsNoDuplicateRows1 = testSpreadsheetFromJsonNoDuplicateRowsAllIds1()
        compareSpreadsheets(testSpreadsheetFromJsonAllIdsNoDuplicateRows1, spreadsheet)
    }

    @Test
    void importRowsSomeDuplicatesAllowedAllWithIdsIsValidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-from-json-duplicate-rows-all-ids-1.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, true, false)
        assertTrue("Spreadsheet is valid", spreadsheet.isValid(false, false))

        Spreadsheet testSpreadsheetFromJsonDuplicateRowsAllIds1 = testSpreadsheetFromJsonDuplicateRowsAllIds1()
        compareSpreadsheets(testSpreadsheetFromJsonDuplicateRowsAllIds1, spreadsheet)
    }

    @Test(expected = SipProcessingException.class)
    void importRowsSomeDuplicatesNotAllowedAllWithIdsIsInvalidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-from-json-duplicate-rows-all-ids-1.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, false, false)
        assertFalse("Spreadsheet is valid", spreadsheet.isValid(false, false))

        Spreadsheet testSpreadsheetFromJsonDuplicateRowsAllIds1 = testSpreadsheetFromJsonDuplicateRowsAllIds1()
        compareSpreadsheets(testSpreadsheetFromJsonDuplicateRowsAllIds1, spreadsheet)
    }

    @Test
    void importRowsNoDuplidatesSomeWithoutIdsAllowedIsValidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-from-json-no-duplicate-rows-missing-ids-1.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, false, true)
        assertTrue("Spreadsheet is valid", spreadsheet.isValid(false, false))

        Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsMissingIds1 = testSpreadsheetFromJsonNoDuplicateRowsMissingIds1()
        compareSpreadsheets(testSpreadsheetFromJsonNoDuplicateRowsMissingIds1, spreadsheet)
    }

    @Test(expected = SipProcessingException.class)
    void importRowsNoDuplicatesSomeWithoutIdsNotAllowedIsInvalidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-from-json-no-duplicate-rows-missing-ids-1.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, false, false)
        assertFalse("Spreadsheet is valid", spreadsheet.isValid(false, false))

        Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsMissingIds1 = testSpreadsheetFromJsonNoDuplicateRowsMissingIds1()
        compareSpreadsheets(testSpreadsheetFromJsonNoDuplicateRowsMissingIds1, spreadsheet)
    }

    @Test(expected = SipProcessingException.class)
    void importJsonFileThatIsWrongJsonStructureIsInvalidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-wrong-structure-json.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, true, true)
        assertFalse("Spreadsheet is valid", spreadsheet.isValid(false, false))
    }


    @Test(expected = SipProcessingException.class)
    void importJsonFileThatIsNotJsonIsInvalidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-not-valid-json.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, true, true)
        assertTrue("Spreadsheet is valid", spreadsheet.isValid(false, false))
    }

    Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsAllIds1() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, false)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFromJsonDuplicateRowsAllIds1() {
        List<Map<String, String>> rows = ROWS_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, true, true)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsMissingIds1() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', rows, false, true)

        return spreadsheet
    }

    void compareSpreadsheets(Spreadsheet expectedSpreadsheet, Spreadsheet actualSpreadsheet) {
        expectedSpreadsheet.rows.eachWithIndex { Map<String, String> lineMap, int index ->
            compareMaps(lineMap, actualSpreadsheet.rows.get(index))
        }

        actualSpreadsheet.rows.eachWithIndex { Map<String, String> lineMap, int index ->
            compareMaps(lineMap, expectedSpreadsheet.rows.get(index))
        }
    }

    void compareMaps(Map<String, String> expectedMap, Map<String, String> actualMap) {
        assertThat("Same number of entries", actualMap.size(), is(expectedMap.size()))

        actualMap.each { String key, String value ->
            assertTrue("Same entry as actual for key=${key}, value=${value}", mapHasEntry(expectedMap, key, value))
        }

        expectedMap.each { String key, String value ->
            assertTrue("Same entries as expected for key=${key}, value=${value}", mapHasEntry(actualMap, key, value))
        }
    }

    boolean mapHasEntry(Map<String, String> map, String key, String value) {
        return map.get(key) == value
    }

    /**
     * This is simply to create test spreadsheet files.
     */
    void generateJsonSpreadsheets() {
        Spreadsheet spreadsheet = testSpreadsheetFromJsonNoDuplicateRowsAllIds1()
        println("\n\ntestSpreadsheetFromJsonAllIdsNoDuplicateRows1:")
        println("${JsonOutput.prettyPrint(spreadsheet.asJsonString())}")

        spreadsheet = testSpreadsheetFromJsonDuplicateRowsAllIds1()
        println("\n\ntestSpreadsheetFromJsonDuplicateRowsAllIds1:")
        println("${JsonOutput.prettyPrint(spreadsheet.asJsonString())}")

        spreadsheet = testSpreadsheetFromJsonNoDuplicateRowsMissingIds1()
        println("\n\ntestSpreadsheetFromJsonNoDuplicateRowsMissingIds1:")
        println("${JsonOutput.prettyPrint(spreadsheet.asJsonString())}")
    }
}
