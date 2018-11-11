package nz.govt.natlib.tools.sip.generation.parameters

import groovy.json.JsonException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Canonical
import groovy.util.logging.Slf4j
import nz.govt.natlib.tools.sip.SipProcessingException

@Slf4j
@Canonical
class Spreadsheet {
    static String NO_ID_KEY = "NO_GIVEN_ID"

    /** The column to use for ids */
    String idColumnName
    boolean allowDuplicateIds
    boolean allowRowsWithoutIds
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
        List<Map<String, Map<String, String>>> jsonList = (List<Map<String, Map<String, String>>>) parsedJson

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

        return new Spreadsheet(idColumnName, rowsForCreation, allowDuplicateIds, allowRowsWithoutIds)
    }

    Spreadsheet(String idColumnName, List<Map<String, String>> rows, boolean allowDuplicateIds = false,
                boolean allowRowsWithoutIds = false) throws SipProcessingException {
        if (idColumnName == null || idColumnName.isEmpty()) {
            throw new IllegalArgumentException("idColumnName='${idColumnName}' cannot be null or empty.")
        }
        if (rows == null) {
            throw new IllegalArgumentException("Spreadsheet rows=${rows} cannot be null.")
        }
        this.idColumnName = idColumnName
        this.allowDuplicateIds = allowDuplicateIds
        this.allowRowsWithoutIds = allowRowsWithoutIds
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
            stringBuilder.append(rowString(columnId, row), withColumnId)
        }
        return stringBuilder
    }

    List<Map<String, Map<String, String>>> keyOrNoIdMap() {
        List<Map<String, Map<String, String>>> keyOrNoIdMap = [ ]
        rows.each { Map<String, String> rowMap ->
            String idKey = rowMap.get(idColumnName)
            if (idKey == null) {
                keyOrNoIdMap.add([ "${NO_ID_KEY}": rowMap ])
            } else {
                keyOrNoIdMap.add([ "${idKey}": rowMap ])
            }
        }
        return keyOrNoIdMap
    }

    String asJsonString() {
        String jsonOutput = JsonOutput.toJson(keyOrNoIdMap())
        return jsonOutput
    }
}
