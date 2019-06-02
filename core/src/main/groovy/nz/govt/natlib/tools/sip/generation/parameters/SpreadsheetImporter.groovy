package nz.govt.natlib.tools.sip.generation.parameters

import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.state.SipProcessingException

import java.util.regex.Pattern

@Log4j2
class SpreadsheetImporter {

    static Spreadsheet extractSpreadsheet(File spreadsheetSourceFile, String idColumnName, String separator = ",",
                                          boolean allowDuplicateIds = false, boolean allowRowsWithoutIds = false)
            throws SipProcessingException {
        if (!spreadsheetSourceFile.exists() || !spreadsheetSourceFile.isFile()) {
            throw new SipProcessingException("Spreasheet-source-file=${spreadsheetSourceFile.getCanonicalPath()} does not exist or is not a file. Extraction cannot continue")
        }
        return extractSpreadsheet(spreadsheetSourceFile.newInputStream(), idColumnName, separator, allowDuplicateIds,
                allowRowsWithoutIds)
    }

    static Spreadsheet extractSpreadsheet(String csvString, String idColumnName, String separator = ",",
                                          boolean allowDuplicateIds = false, boolean allowRowsWithoutIds = false)
            throws SipProcessingException {
        InputStream inputStream = new ByteArrayInputStream(csvString.getBytes('UTF-8'))
        return extractSpreadsheet(inputStream, idColumnName, separator, allowDuplicateIds, allowRowsWithoutIds)
    }

    static Spreadsheet extractSpreadsheet(InputStream inputStream, String idColumnName, String separator = ",",
                                          boolean allowDuplicateIds = false, boolean allowRowsWithoutIds = false)
            throws SipProcessingException {
        List<Map<String, String>> spreadsheetRows = [ ]
        int lineCounter = 0
        List<String> columnHeaders = [ ]
        List<String> comments = [ ]
        // Quote any special regex characters in the separator
        // TODO Maybe have a regex method to allow more varied splitting?
        String regexPattern = Pattern.quote(separator)
        inputStream.eachLine() { String line ->
            boolean updateCounter = true
            if (line.trim().startsWith("#")) {
                comments.add(line)
                updateCounter = false
            } else if (lineCounter == 0) {
                columnHeaders = line.split(regexPattern).collect { String columnHeader ->
                    columnHeader.trim()
                }
            } else {
                List<String> rowValues = line.split(regexPattern).collect { String rowCell ->
                    rowCell.trim()
                }
                Map<String, String> rowMap = [ : ]
                columnHeaders.eachWithIndex() { String columnHeader, int index ->
                    if (rowValues.size() > index) {
                        rowMap.put(columnHeader, rowValues.get(index))
                    } else {
                        rowMap.put(columnHeader, Spreadsheet.EMPTY_VALUE)
                    }
                }
                // For rows without a column header
                if (rowValues.size() > columnHeaders.size()) {
                    for (int index = columnHeaders.size(); index < rowValues.size(); index++) {
                        // NOTE: This will wipe out a previous value with the same key
                        rowMap.put('', rowValues.get(index))
                    }
                }
                spreadsheetRows.add(rowMap)
            }
            if (updateCounter) {
                lineCounter += 1
            }
        }
        Spreadsheet spreadsheet = new Spreadsheet(idColumnName, columnHeaders, spreadsheetRows, comments,
                allowDuplicateIds, allowRowsWithoutIds)
        return spreadsheet
    }

}
