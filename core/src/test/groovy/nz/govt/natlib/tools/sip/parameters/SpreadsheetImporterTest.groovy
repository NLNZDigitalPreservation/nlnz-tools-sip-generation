package nz.govt.natlib.tools.sip.parameters

import nz.govt.natlib.tools.sip.generation.parameters.Spreadsheet
import nz.govt.natlib.tools.sip.generation.parameters.SpreadsheetImporter

import static org.hamcrest.core.Is.is
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
 * Tests {@link SpreadsheetImporter}.
 */
@RunWith(MockitoJUnitRunner.class)
class SpreadsheetImporterTest {
    static TEST_SPREADSHEET_1_MAP = [
            [ 'column-1': 'row1', 'column 2': 'second', 'column,3': 'third', 'a column 4': 'fourth' ],
            [ 'column-1': 'row2', 'column 2': 'another' ],
            [ 'column-1': 'row3', 'column 2': 'c2', 'column,3': 'c3', 'a column 4': 'c4' ]
    ]

    Spreadsheet testSpreadsheet1 = new Spreadsheet("column-1", TEST_SPREADSHEET_1_MAP)

    @Test
    void loadsTestSpreadsheet1Correctly() {
        InputStream spreadsheetInputStream = SpreadsheetImporterTest.getResourceAsStream("test-spreadsheet-1.csv")
        Spreadsheet loadedSpreadsheet = SpreadsheetImporter.extractSpreadsheet(spreadsheetInputStream,
                "column-1", "|")

        assertThat("All lines read in", new Integer(loadedSpreadsheet.rows.size()), is(3))
        assertThat("Same idColumnName", testSpreadsheet1.idColumnName, is(loadedSpreadsheet.idColumnName))
        compareSpreadsheets(testSpreadsheet1, loadedSpreadsheet)
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
