package nz.govt.natlib.tools.sip.generation.parameters

import groovy.json.JsonException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Canonical
import groovy.util.logging.Slf4j
import nz.govt.natlib.tools.sip.state.SipProcessingException

@Slf4j
@Canonical
class Spreadsheet {
    static final String NO_ID_KEY = "NO_GIVEN_ID"
    static final String COLUMN_HEADERS_KEY = "COLUMN_NAMES"
    static final String COMMENTS_KEY = "COMMENTS"
    static final String EMPTY_VALUE = ""

    /** The column to use for ids */
    String idColumnName
    boolean allowDuplicateIds
    boolean allowRowsWithoutIds
    // Comments start with a '#'
    List<String> comments = [ ]
    List<String> columnHeaders = [ ]
    List<Map<String, String>> rows = [ ]

    static Spreadsheet fromJson(String idColumnName, File jsonFile, boolean allowDuplicateIds = false,
                                boolean allowRowsWithoutIds = false) throws SipProcessingException {
        return fromJson(idColumnName, jsonFile.text, allowDuplicateIds, allowRowsWithoutIds)
    }

    static Spreadsheet fromJson(String idColumnName, String jsonString, boolean allowDuplicateIds = false,
                                boolean allowRowsWithoutIds = false) throws SipProcessingException {
        JsonSlurper jsonSlurper = new JsonSlurper()
        def parsedJson
        try {
            parsedJson = jsonSlurper.parseText(jsonString)
        } catch (IllegalArgumentException | JsonException e) {
            throw new SipProcessingException("Unable to parse JSON-text='${jsonString}'")
        }
        // We assume the JSON is the same structure as produced by {@link #asJsonString}.
        List<Object> simpleJsonList = (List<Object>) parsedJson
        List<String> theComments = [ ]
        List<String> theColumnHeaders = [ ]
        List<Map<String, Map<String, String>>> jsonList = [ ]
        simpleJsonList.each { Object jsonRow ->
            Map<String, Object> jsonRowMap = (Map<String, Object>) jsonRow
            if (jsonRowMap.containsKey(COMMENTS_KEY)) {
                theComments = (List<String>) jsonRowMap.get(COMMENTS_KEY)
            } else if (jsonRowMap.containsKey(COLUMN_HEADERS_KEY)) {
                theColumnHeaders = (List<String>) jsonRowMap.get(COLUMN_HEADERS_KEY)
            } else {
                jsonList.add((Map<String, Map<String, String>>) jsonRow)
            }
        }

        List<Map<String, String>> rowsForCreation = [ ]
        try {
            jsonList.each { Map<String, Map<String, String>> idToRowMap ->
                // Generally there is only 1 key in the map, but there could be multiple
                idToRowMap.each { String key, Map<String, String> rowKeyValueMap ->
                    rowsForCreation.add(rowKeyValueMap)
                }
            }
        } catch (IllegalArgumentException | MissingMethodException e) {
            throw new SipProcessingException("Unable to convert to proper input format: JSON-text='${jsonString}'")
        }

        return new Spreadsheet(idColumnName, theColumnHeaders, rowsForCreation, theComments,
                allowDuplicateIds, allowRowsWithoutIds)
    }

    Spreadsheet(String idColumnName, List<String> columnHeaders, List<Map<String, String>> rows,
                List<String> comments = [ ], boolean allowDuplicateIds = false, boolean allowRowsWithoutIds = false)
            throws SipProcessingException {
        if (idColumnName == null || idColumnName.isEmpty()) {
            throw new IllegalArgumentException("idColumnName='${idColumnName}' cannot be null or empty.")
        }
        if (rows == null) {
            throw new IllegalArgumentException("Spreadsheet rows=${rows} cannot be null.")
        }
        this.idColumnName = idColumnName
        this.allowDuplicateIds = allowDuplicateIds
        this.allowRowsWithoutIds = allowRowsWithoutIds
        this.columnHeaders = columnHeaders
        this.comments = comments
        this.rows = rows
        if (!isValid(true, true)) {
            throw new SipProcessingException("Spreadsheet is invalid. See warnings in log.")
        }
    }

    Map<String, String> getRow(String id) {
        Map<String, String> row = rows.find { Map<String, String> candidateRow ->
            candidateRow.get(id) != null
        }
        return row
    }

    boolean isValid(boolean showDuplicates = true, boolean showRowsWithoutIds = true) {
        boolean hasDuplicates = false
        boolean hasRowsWithoutIds = false
        if (!allowDuplicateIds || showDuplicates) {
            Map<String, List<Map<String, String>>> duplicateKeysRowsMap = duplicateKeysWithRows()
            if (duplicateKeysRowsMap.size() > 0) {
                hasDuplicates = true
                if (showDuplicates) {
                    log.warn("Duplicate row ids for spreadsheet")
                    duplicateKeysRowsMap.each { String columnId, List<Map<String, String>> rows ->
                        log.warn("columnId: '${columnId}':")
                        rows.each { Map<String, String> row ->
                            log.warn("    ${rowString(row.get(idColumnName), row, false)}")
                        }
                    }
                }
            }
        }
        List<Map<String, String>> rowsWithoutIds = rowsWithoutIds()
        if (rowsWithoutIds.size() > 0) {
            hasRowsWithoutIds = true
            if (showRowsWithoutIds) {
                log.warn("Rows without values corresponding to column-name='${idColumnName}':")
                rowsWithoutIds.each { Map<String, String> row ->
                    log.warn("    ${rowString((String) null, row, false)}")
                }
            }
        }
        boolean isInvalid = (hasDuplicates && !allowDuplicateIds) || (hasRowsWithoutIds && !allowRowsWithoutIds)
        return !isInvalid
    }

    Map<String, List<Map<String, String>>> duplicateKeysWithRows() {
        Map<String, List<Map<String, String>>> idToRowsMap = [ : ]
        rows.each { Map<String, String> row ->
            String rowId = row.get(idColumnName)
            if (rowId != null) {
                if (idToRowsMap.get(rowId) == null) {
                    idToRowsMap.put(rowId, [row])
                } else {
                    idToRowsMap.get(rowId).add(row)
                }
            } else {
                // skip it, we will catch it in rowsWithoutIds
            }
        }
        Map<String, List<Map<String, String>>> duplicates = [ : ]
        idToRowsMap.each { String columnId, List<Map<String, String>> rowsWithColumnId ->
            if (rowsWithColumnId.size() > 1) {
                duplicates.put(columnId, rowsWithColumnId)
            }
        }
        return duplicates
    }

   List<Map<String, String>> rowsWithoutIds() {
        List<Map<String, String>> rowsWithoutIdsList = [ ]
        rows.each { Map<String, String> row ->
            String rowId = row.get(idColumnName)
            if (rowId == null) {
                rowsWithoutIdsList.add(row)
            }
        }
        return rowsWithoutIdsList
    }

    List<Map<String, String>> mapsForId(String id) {
        List<Map<String, String>> mapsForId = []
        rows.each { Map<String, String> row ->
            String rowId = row.get(idColumnName)
            if (rowId != null && rowId == id) {
                mapsForId.add(row)
            }
        }
        return mapsForId
    }

    String rowString(String columnId, Map<String, String> row, boolean withColumnId) {
        StringBuilder stringBuilder = new StringBuilder()
        if (withColumnId) {
            stringBuilder.append("${columnId}: ")
        }
        boolean firstRow = true
        row.each { String key, String value ->
            if (!firstRow) {
                stringBuilder.append(", ")
            }
            stringBuilder.append("'${key}': '${value}'")
            firstRow = false
        }
        return stringBuilder.toString()
    }

    String display(boolean withColumnId) {
        StringBuilder stringBuilder = new StringBuilder()
        rows.each { Map<String, String> row ->
            String columnId = row.get(this.idColumnName)
            stringBuilder.append(rowString(columnId, row, withColumnId))
        }
        return stringBuilder
    }

    List<Map<String, Map<String, String>>> keyOrNoIdMap(boolean orderByColumnHeaders = true) {
        List<Map<String, Map<String, String>>> keyOrNoIdMap = [ ]
        rows.each { Map<String, String> rowMap ->
            Map<String, String> adjustedRowMap = rowMap
            String idKey = rowMap.get(idColumnName)
            if (orderByColumnHeaders) {
                adjustedRowMap = new LinkedHashMap<String, String>()
                this.columnHeaders.each { String columnHeader ->
                    String columnValue = rowMap.get(columnHeader)
                    if (columnValue == null) {
                        columnValue = EMPTY_VALUE
                    }
                    adjustedRowMap.put(columnHeader, columnValue)
                }
                unmappedColumns(rowMap).each { String unmappedKey ->
                    adjustedRowMap.put(unmappedKey, rowMap.get(unmappedKey))
                }
            }
            if (idKey == null) {
                keyOrNoIdMap.add([ ("${NO_ID_KEY}".toString()): adjustedRowMap ])
            } else {
                keyOrNoIdMap.add([ ("${idKey}".toString()): adjustedRowMap ])
            }
        }
        return keyOrNoIdMap
    }

    List<String> unmappedColumns(Map<String, String> rowMap) {
        List<String> unmapped = [ ]
        Set<String> columnHeadersSet = [ ]
        columnHeadersSet.addAll(this.columnHeaders)
        rowMap.keySet().each { String key ->
            if (!columnHeadersSet.contains(key)) {
                unmapped.add(key)
            }
        }
        return unmapped
    }

    String asJsonString() {
        List<Object> jsonList = [ ]
        jsonList.add(("${COMMENTS_KEY}".toString()): comments)
        jsonList.add(("${COLUMN_HEADERS_KEY}".toString()): columnHeaders)
        jsonList.addAll(keyOrNoIdMap())
        String jsonOutput = JsonOutput.toJson(jsonList)
        return jsonOutput
    }

    String asCsvString(String separator = ",") {
        StringBuilder stringBuilder = new StringBuilder()
        this.comments.each { String comment ->
            stringBuilder.append(comment)
            stringBuilder.append(System.lineSeparator())
        }
        boolean prependSeparator = false
        this.columnHeaders.each { String columnHeader ->
            if (prependSeparator) {
                stringBuilder.append(separator)
            } else {
                prependSeparator = true
            }
            stringBuilder.append(columnHeader)
        }
        stringBuilder.append(System.lineSeparator())

        this.rows.each { Map<String, String> row ->
            prependSeparator = false
            this.columnHeaders.each { String columnHeader ->
                String columnValue = row.get(columnHeader)
                if (columnValue == null) {
                    columnValue = EMPTY_VALUE
                }
                if (prependSeparator) {
                    stringBuilder.append(separator)
                } else {
                    prependSeparator = true
                }
                stringBuilder.append(columnValue)
            }
            unmappedColumns(row).each { String unmappedColumn ->
                if (prependSeparator) {
                    stringBuilder.append(separator)
                } else {
                    prependSeparator = true
                }
                stringBuilder.append(unmappedColumn)
            }
            stringBuilder.append(System.lineSeparator())
        }
        return stringBuilder.toString()
    }

}
