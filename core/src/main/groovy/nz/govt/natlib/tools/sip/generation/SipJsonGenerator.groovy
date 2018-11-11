package nz.govt.natlib.tools.sip.generation

import groovy.json.JsonGenerator
import groovy.json.JsonGenerator.Converter
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import nz.govt.natlib.tools.sip.Sip

import java.time.LocalDateTime

@Slf4j
class SipJsonGenerator {

    static String toJson(Sip sip, boolean prettyPrint = false) {
        JsonGenerator jsonGenerator = new JsonGenerator.Options()
                .excludeFieldsByName([ 'localDate', 'LOCAL_DATE_TIME_FORMATTER' ])  // Exclude fields with given name(s).
                .addConverter(new Converter() {
                    // LocalDateTime converter
                    boolean handles(Class<?> type) {
                        return LocalDateTime.isAssignableFrom(type)
                    }
                    Object convert(Object localDateTime, String key) {
                        return Sip.LOCAL_DATE_TIME_FORMATTER.format((LocalDateTime) localDateTime)
                    }

                })
                .addConverter(new Converter() {
                    // File converter
                    boolean handles(Class<?> type) {
                        return File.isAssignableFrom(type)
                    }
                    Object convert(Object file, String key) {
                        File theFile = (File) file
                        return theFile.getCanonicalPath()
                    }
                })
                .build()  // Create the converter instance.

        String jsonString = jsonGenerator.toJson(sip)

        return prettyPrint ? JsonOutput.prettyPrint(jsonString) : jsonString
     }
}
