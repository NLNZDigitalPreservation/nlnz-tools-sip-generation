package nz.govt.natlib.tools.sip.parameters

import nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet
import nz.govt.natlib.tools.sip.generation.parameters.SpreadsheetImporter

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import org.junit.Test

/**
 * Tests {@link SpreadsheetImporter}.
 */
class SpreadsheetImporterTest {
    static TEST_SPREADSHEET_1_MAP = [
            [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ],
            [ 'column-1': 'row2', 'column 2': 'another', 'column,3': '', 'a column 4': '' ],
            [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4' ]
    ]
    static TEST_SPREADSHEET_2_COMMENTS = [ '# Comment 1', '  # Comment 2', '#', '# Comment 4']
    static TEST_SPREADSHEET_2_COLUMN_HEADERS = [ 'column-1', 'column 2', 'column,3', 'a column 4' ]
    static TEST_SPREADSHEET_2_MAP = [
            [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ],
            [ 'column-1': 'row2', 'column 2': 'another', 'column,3': '', 'a column 4': '' ],
            [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4' ]
    ]
    static TEST_SPREADSHEET_3_COMMENTS = [ '# Comment 1', '  # Comment 2', '#', '# Comment 4']
    static TEST_SPREADSHEET_3_COLUMN_HEADERS = [ 'column-1', 'column 2', 'column,3', 'a column 4' ]
    static TEST_SPREADSHEET_3_MAP = [
            [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ],
            [ 'column-1': 'row2', 'column 2': 'another', 'column,3': '', 'a column 4': '' ],
            [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': '', '': '', '': 'column-6' ],
            [ 'column-1': 'row4', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4', '': 'extra-column1', '': 'extra column 2' ]
    ]


    Spreadsheet testSpreadsheet1 = new Spreadsheet("column-1", [ ], TEST_SPREADSHEET_1_MAP)
    Spreadsheet testSpreadsheet2 = new Spreadsheet('column-1', TEST_SPREADSHEET_2_COLUMN_HEADERS,
            TEST_SPREADSHEET_2_MAP, TEST_SPREADSHEET_2_COMMENTS, false, false)
    Spreadsheet testSpreadsheet3 = new Spreadsheet('column-1', TEST_SPREADSHEET_3_COLUMN_HEADERS,
            TEST_SPREADSHEET_3_MAP, TEST_SPREADSHEET_3_COMMENTS, false, false)

    @Test
    void loadsTestSpreadsheet1Correctly() {
        InputStream spreadsheetInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-1.csv")
        Spreadsheet loadedSpreadsheet = SpreadsheetImporter.extractSpreadsheet(spreadsheetInputStream,
                "column-1", "|")

        assertThat("All lines read in", new Integer(loadedSpreadsheet.rows.size()), is(3))
        assertThat("Same idColumnName", testSpreadsheet1.idColumnName, is(loadedSpreadsheet.idColumnName))
        assertTrue("No comments", testSpreadsheet1.comments.isEmpty())
        compareSpreadsheets(testSpreadsheet1, loadedSpreadsheet)
    }

    @Test
    void loadsTestSpreadsheet2Correctly() {
        InputStream spreadsheetInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-2.csv")
        Spreadsheet loadedSpreadsheet = SpreadsheetImporter.extractSpreadsheet(spreadsheetInputStream,
                "column-1", "|")

        assertThat("All lines read in", new Integer(loadedSpreadsheet.rows.size()), is(3))
        assertThat("Column headers match", testSpreadsheet2.columnHeaders, is(TEST_SPREADSHEET_2_COLUMN_HEADERS))
        assertThat("Comments match", testSpreadsheet2.comments, is(TEST_SPREADSHEET_2_COMMENTS))
        assertThat("Same idColumnName", testSpreadsheet2.idColumnName, is(loadedSpreadsheet.idColumnName))
        compareSpreadsheets(testSpreadsheet2, loadedSpreadsheet)
    }

    @Test
    void loadsTestSpreadsheet3Correctly() {
        InputStream spreadsheetInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-3.csv")
        Spreadsheet loadedSpreadsheet = SpreadsheetImporter.extractSpreadsheet(spreadsheetInputStream,
                "column-1", "|")

        assertThat("All lines read in", new Integer(loadedSpreadsheet.rows.size()), is(4))
        assertThat("Column headers match", testSpreadsheet3.columnHeaders, is(TEST_SPREADSHEET_3_COLUMN_HEADERS))
        assertThat("Comments match", testSpreadsheet3.comments, is(TEST_SPREADSHEET_3_COMMENTS))
        assertThat("Same idColumnName", testSpreadsheet3.idColumnName, is(loadedSpreadsheet.idColumnName))
        compareSpreadsheets(testSpreadsheet3, loadedSpreadsheet)
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
}
