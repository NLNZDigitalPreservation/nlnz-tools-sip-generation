package nz.govt.natlib.tools.sip.extraction

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import nz.govt.natlib.tools.sip.Sip

import java.time.LocalDateTime

@Slf4j
class SipJsonExtractor {
    final String json

    SipJsonExtractor(String json) {
        this.json = json
    }

    Sip asSip() {
        Map<String, Object> parsedMap = (Map<String, Object>) new JsonSlurper().parseText(this.json)
        log.info("Parsed input JSON=${this.json} into parsedMap=${parsedMap}")

        List<Map<String, Object>> fileWrappers = (List<Map<String, Object>>) parsedMap.get("fileWrappers")

        List<Sip.FileWrapper> replacementFileWrappers = [ ]

        fileWrappers.each { Map<String, Object> fileWrapper ->
            fileWrapper.creationDate = LocalDateTime.parse((String) fileWrapper.creationDate, Sip.LOCAL_DATE_TIME_FORMATTER)
            fileWrapper.modificationDate = LocalDateTime.parse((String) fileWrapper.modificationDate, Sip.LOCAL_DATE_TIME_FORMATTER)
            if (fileWrapper.file != null) {
                fileWrapper.file = new File((String) fileWrapper.file)
            }

            Sip.FileWrapper replacementWrapper = new Sip.FileWrapper(fileWrapper)
            replacementFileWrappers.add(replacementWrapper)
        }
        parsedMap.put("fileWrappers", replacementFileWrappers)

        Sip sip = new Sip(parsedMap)

        log.info("JSON converted to Sip=${sip}")

        return sip
    }
}
