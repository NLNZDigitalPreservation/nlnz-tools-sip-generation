package nz.govt.natlib.tools.sip.generation.parameters

import nz.govt.natlib.tools.sip.generation.SipGenerationException

import java.util.regex.Pattern

class SpreadsheetImporter {

    static Spreadsheet extractSpreadsheet(File spreadsheetSourceFile, String idColumnName, String separator = ",",
                                          boolean allowDuplicateIds = false, boolean allowRowsWithoutIds = false)
            throws SipGenerationException {
        if (!spreadsheetSourceFile.exists() || !spreadsheetSourceFile.isFile()) {
            throw new SipGenerationException("Spreasheet-source-file=${spreadsheetSourceFile.getCanonicalPath()} does not exist or is not a file. Extraction cannot continue")
        }
        return extractSpreadsheet(spreadsheetSourceFile.newInputStream(), idColumnName, separator, allowDuplicateIds,
                allowRowsWithoutIds)
    }

    static Spreadsheet extractSpreadsheet(InputStream inputStream, String idColumnName, String separator = ",",
                                          boolean allowDuplicateIds = false, boolean allowRowsWithoutIds = false)
            throws SipGenerationException {
        List<Map<String, String>> spreadsheetRows = [ ]
        int lineCounter = 0
        List<String> columnHeaders = [ ]
        // Quote any special regex characters in the separator
        // TODO Maybe have a regex method to allow more varied splitting?
        String regexPattern = Pattern.quote(separator)
        inputStream.eachLine() { String line ->
            if (lineCounter == 0) {
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
                    }
                }
                spreadsheetRows.add(rowMap)
            }
            lineCounter += 1
        }
        Spreadsheet spreadsheet = new Spreadsheet(idColumnName, spreadsheetRows, allowDuplicateIds, allowRowsWithoutIds)
        return spreadsheet
    }

}
