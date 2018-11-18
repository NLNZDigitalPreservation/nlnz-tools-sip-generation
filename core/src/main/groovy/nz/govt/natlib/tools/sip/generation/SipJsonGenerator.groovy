package nz.govt.natlib.tools.sip.generation

import groovy.json.JsonBuilder

//import groovy.json.JsonGenerator
//import groovy.json.JsonGenerator.Converter
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import nz.govt.natlib.tools.sip.Sip

//import java.time.LocalDateTime

@Slf4j
class SipJsonGenerator {

    static String toJson(Sip sip, boolean prettyPrint = false) {
        // groovy 2.5.x u
        JsonBuilder jsonBuilder = new JsonBuilder()
        Map<String, Object> jsonRoot = jsonBuilder {
            title sip.title
            year sip.year
            month sip.month
            dayOfMonth sip.dayOfMonth

            ieEntityType sip.ieEntityType
            objectIdentifierType sip.objectIdentifierType
            objectIdentifierValue sip.objectIdentifierValue
            policyId sip.policyId

            preservationType sip.preservationType
            usageType sip.usageType
            digitalOriginal sip.digitalOriginal
            revisionNumber sip.revisionNumber
            almaMmsId sip.almaMmsId

            fileWrappers(sip.fileWrappers) { Sip.FileWrapper fileWrapper ->
                    mimeType fileWrapper.mimeType
                    file fileWrapper.file == null ? null : fileWrapper.file.getCanonicalPath()
                    fileOriginalPath fileWrapper.fileOriginalPath
                    fileOriginalName fileWrapper.fileOriginalName
                    label fileWrapper.label
                    creationDate sip.LOCAL_DATE_TIME_FORMATTER.format(fileWrapper.creationDate)
                    modificationDate sip.LOCAL_DATE_TIME_FORMATTER.format(fileWrapper.modificationDate)
                    fileSizeBytes fileWrapper.fileSizeBytes
                    fixityType fileWrapper.fixityType
                    fixityValue fileWrapper.fixityValue
            }
        }

        String jsonString = jsonBuilder.toString()

        String outputString = prettyPrint ? JsonOutput.prettyPrint(jsonString) : jsonString

        log.debug("sip=${sip} generated json=${outputString}")

        return outputString
     }

    /**
     * Groovy 2.5.x supports JsonGenerator, which provides a much more elegant way to generate JSON from an object
     * using Converter classes. But gradle 4.10.x uses groovy 2.4, which does not have these classes. Conflicts with
     * gradle builds means that it's not easy to dependent builds with groovy 2.5 classes. For this reason, this
     * method is commented out until Gradle runs with groovy 2.5.x.
     */
//    static String toJson(Sip sip, boolean prettyPrint = false) {
//        // groovy 2.5.x u
//        JsonGenerator jsonGenerator = new JsonGenerator.Options()
//                .excludeFieldsByName([ 'localDate', 'LOCAL_DATE_TIME_FORMATTER' ])  // Exclude fields with given name(s).
//                .addConverter(new Converter() {
//            // LocalDateTime converter
//            boolean handles(Class<?> type) {
//                return LocalDateTime.isAssignableFrom(type)
//            }
//            Object convert(Object localDateTime, String key) {
//                return Sip.LOCAL_DATE_TIME_FORMATTER.format((LocalDateTime) localDateTime)
//            }
//
//        })
//                .addConverter(new Converter() {
//            // File converter
//            boolean handles(Class<?> type) {
//                return File.isAssignableFrom(type)
//            }
//            Object convert(Object file, String key) {
//                File theFile = (File) file
//                return theFile.getCanonicalPath()
//            }
//        })
//                .build()  // Create the converter instance.
//
//        String jsonString = jsonGenerator.toJson(sip)
//
//        return prettyPrint ? JsonOutput.prettyPrint(jsonString) : jsonString
//    }
}
