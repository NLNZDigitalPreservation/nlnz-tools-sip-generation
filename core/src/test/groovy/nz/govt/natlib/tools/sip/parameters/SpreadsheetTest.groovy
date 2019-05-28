package nz.govt.natlib.tools.sip.parameters

import groovy.json.JsonOutput
import nz.govt.natlib.tools.sip.generation.parameters.SpreadsheetImporter
import nz.govt.natlib.tools.sip.state.SipProcessingException
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
    static final List<String> COMMENTS = [ '# Comment 1', '  # Comment 2', '#', '# Comment 4' ]
    static final List<String> NO_COMMENTS = [ ]
    static final List<String> COLUMN_HEADERS = [ 'column-1', 'column 2', 'column,3', 'a column 4']
    static final Map<String, String> ROW_SAMPLE_ONE = [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static final Map<String, String> ROW_SAMPLE_TWO = [ 'column-1': 'row2', 'column 2': 'another' ]
    static final Map<String, String> ROW_SAMPLE_TWO_ALL_COLUMNS = [ 'column-1': 'row2', 'column 2': 'another', 'column,3': '', 'a column 4': '' ]
    static final Map<String, String> ROW_SAMPLE_TWO_EMPTY_3 = [ 'column-1': 'row2', 'column 2': 'another', 'column,3': '', 'a column 4': 'four had an empty before' ]
    static final Map<String, String> ROW_SAMPLE_THREE = [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4' ]
    static final Map<String, String> ROW_SAMPLE_THREE_TO_COLUMN_6 = [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': '', '': 'column-6' ]
    static final Map<String, String> ROW_SAMPLE_DUPLICATE_ONE = [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static final Map<String, String> ROW_SAMPLE_NO_COLUMN_1 = [ 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static final Map<String, String> ROW_SAMPLE_EMPTY_COLUMN_1 = [ 'column-1': '', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ]
    static final Map<String, String> ROW_SAMPLE_FOUR_TO_COLUMN_6 = [ 'column-1': 'row4', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4', '': 'extra column 2' ]

    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_ALL_IDS_1 = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE ]
    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_ALL_IDS_ALL_COLUMNS_1 = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO_ALL_COLUMNS, ROW_SAMPLE_THREE ]
    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_ALL_IDS_ALL_COLUMNS_TWO_EMPTY_3_1 = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO_EMPTY_3, ROW_SAMPLE_THREE ]
    static List<Map<String, String>> ROWS_DUPLICATE_ROWS_ALL_IDS_1 = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_DUPLICATE_ONE ]
    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1 = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE, ROW_SAMPLE_NO_COLUMN_1 ]
    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_ALL_COLUMNS_1 = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO_ALL_COLUMNS, ROW_SAMPLE_THREE, ROW_SAMPLE_EMPTY_COLUMN_1 ]
    static List<Map<String, String>> ROWS_NO_DUPLICATE_ROWS_ALL_IDS_WITH_EXTRAS = [ ROW_SAMPLE_ONE, ROW_SAMPLE_TWO, ROW_SAMPLE_THREE_TO_COLUMN_6, ROW_SAMPLE_FOUR_TO_COLUMN_6 ]

    @Test
    void rowsWithIdsAreValidSpreadsheet() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, COMMENTS, false, false)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has no rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() == 0)
        assertTrue("Spreadsheet has no rows without ids", spreadsheet.rowsWithoutIds().size() == 0)
    }

    @Test(expected = SipProcessingException.class)
    void rowsWithDuplicateIdsAreInvalidIfNotAllowed() {
        List<Map<String, String>> rows = ROWS_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, NO_COMMENTS, false, false)

        assertTrue("Spreadsheet is invalid", !spreadsheet.isValid())
        assertTrue("Spreadsheet has rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() > 0)
    }

    @Test
    void rowsWithDuplicateIdsAreValidIfAllowed() {
        List<Map<String, String>> rows = ROWS_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, NO_COMMENTS, true, false)

        assertTrue("Spreadsheet is valid", spreadsheet.isValid())
        assertTrue("Spreadsheet has rows with duplicate ids", spreadsheet.duplicateKeysWithRows().size() > 0)
    }

    @Test(expected = SipProcessingException.class)
    void rowsWithoutIdsAreInvalidIfNotAllowed() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, NO_COMMENTS, false, false)

        assertTrue("Spreadsheet is invalid", !spreadsheet.isValid())
        assertTrue("Spreadsheet has rows without ids", spreadsheet.rowsWithoutIds().size() > 0)
    }

    @Test
    void rowsWithoutIdsAreValidIfAllowed() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, NO_COMMENTS, false, true)

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
    void importRowsNoDuplicatesAllWithIdsColumnHeadersAndCommentsAndExtraColumnsIsValidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-from-json-no-duplicate-rows-all-ids-column-headers-and-comments-1.json")
        String jsonString = jsonFileInputStream.getText()

        // Note that columns without a header will have the same id (an empty string) so duplicate ids must be allowed
        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, true, true)
        assertTrue("Spreadsheet is valid", spreadsheet.isValid(false, false))

        Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsAllIdsColumnHeadersAndComments1 = testSpreadsheetFromJsonNoDuplicateRowsAllIdsColumnHeadersAndComments1()
        compareSpreadsheets(testSpreadsheetFromJsonNoDuplicateRowsAllIdsColumnHeadersAndComments1, spreadsheet)
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

        Spreadsheet spreadsheet = Spreadsheet.fromJson("column-1", jsonString, true, true)
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

    @Test
    void importWithGeneratedRowIdsIsValidSpreadsheet() {
        InputStream jsonFileInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-from-json-generated-row-ids-1.json")
        String jsonString = jsonFileInputStream.getText()

        Spreadsheet spreadsheet = Spreadsheet.fromJson("GENERATE_ID_VALUE", jsonString, true, true)
        assertTrue("Spreadsheet is valid", spreadsheet.isValid(false, false))

        Spreadsheet testSpreadsheetFromJsonGeneratedRowIds1 = testSpreadsheetFromJsonGeneratedRowIds1()
        compareSpreadsheets(testSpreadsheetFromJsonGeneratedRowIds1, spreadsheet)
    }

    @Test
    void fullCycleWithGeneratedRowIdsWorks() {
        Spreadsheet sourceSpreadsheet = testSpreadsheetFromJsonGeneratedRowIdsAllColumns1()
        String testSpreadsheetJsonString = sourceSpreadsheet.asJsonString()
        Spreadsheet recreatedSpreadsheet = Spreadsheet.fromJson("GENERATE_ID_VALUE", testSpreadsheetJsonString, false, false)
        compareSpreadsheets(sourceSpreadsheet, recreatedSpreadsheet)
    }

    @Test
    void spreadsheetWithGeneratedRowIdsCanBeImportedAndExportedToCsvAndRemainTheSame() {
        Spreadsheet sourceSpreadsheet = testSpreadsheetFromJsonGeneratedRowIdsAllColumns1()
        String separator = "|"
        String testSpreadsheetCsvString = sourceSpreadsheet.asCsvString(separator)
        Spreadsheet recreatedSpreadsheet = SpreadsheetImporter.extractSpreadsheet(testSpreadsheetCsvString, "GENERATE_ID_VALUE",
                separator, false, false)
        compareSpreadsheets(sourceSpreadsheet, recreatedSpreadsheet)
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

    @Test
    void spreadsheetCanBeImportedAndExportedToJsonAndRemainTheSame() {
        Spreadsheet sourceSpreadsheet = testSpreadsheetFullCycle()
        String testSpreadsheetJsonString = sourceSpreadsheet.asJsonString()
        Spreadsheet recreatedSpreadsheet = Spreadsheet.fromJson("column-1", testSpreadsheetJsonString, false, false)
        compareSpreadsheets(sourceSpreadsheet, recreatedSpreadsheet)
    }

    @Test
    void spreadsheetCanBeImportedAndExportedToCsvAndRemainTheSame() {
        Spreadsheet sourceSpreadsheet = testSpreadsheetFullCycle()
        String separator = "|"
        String testSpreadsheetCsvString = sourceSpreadsheet.asCsvString(separator)
        Spreadsheet recreatedSpreadsheet = SpreadsheetImporter.extractSpreadsheet(testSpreadsheetCsvString, "column-1",
                separator, false, false)
        compareSpreadsheets(sourceSpreadsheet, recreatedSpreadsheet)
    }

    Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsAllIds1() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, NO_COMMENTS, false, false)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsAllIdsColumnHeadersAndComments1() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_ALL_IDS_WITH_EXTRAS
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, COMMENTS, true, true)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFullCycle() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_ALL_IDS_ALL_COLUMNS_TWO_EMPTY_3_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, COMMENTS, false, false)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFromJsonDuplicateRowsAllIds1() {
        List<Map<String, String>> rows = ROWS_DUPLICATE_ROWS_ALL_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, NO_COMMENTS, true, true)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFromJsonNoDuplicateRowsMissingIds1() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('column-1', COLUMN_HEADERS, rows, NO_COMMENTS, false, true)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFromJsonGeneratedRowIds1() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_1
        Spreadsheet spreadsheet = new Spreadsheet('GENERATE_ID_VALUE', COLUMN_HEADERS, rows, NO_COMMENTS, false, true)

        return spreadsheet
    }

    Spreadsheet testSpreadsheetFromJsonGeneratedRowIdsAllColumns1() {
        List<Map<String, String>> rows = ROWS_NO_DUPLICATE_ROWS_MISSING_IDS_ALL_COLUMNS_1
        Spreadsheet spreadsheet = new Spreadsheet('GENERATE_ID_VALUE', COLUMN_HEADERS, rows, NO_COMMENTS, false, true)

        return spreadsheet
    }

    void compareSpreadsheets(Spreadsheet expectedSpreadsheet, Spreadsheet actualSpreadsheet) {
        assertThat("Column headers match", expectedSpreadsheet.columnHeaders, is(actualSpreadsheet.columnHeaders))
        assertThat("Comments match", expectedSpreadsheet.comments, is(actualSpreadsheet.comments))

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
