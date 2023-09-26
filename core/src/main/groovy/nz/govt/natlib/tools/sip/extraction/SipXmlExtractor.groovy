package nz.govt.natlib.tools.sip.extraction

import groovy.util.logging.Log4j2
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import nz.govt.natlib.tools.sip.IEEntityType
import nz.govt.natlib.tools.sip.Sip

import java.time.LocalDateTime

@Log4j2
class SipXmlExtractor {
    static final Map<String, String> SIP_NAMESPACES = [
            mets: 'http://www.loc.gov/METS/',
            dc: 'http://purl.org/dc/elements/1.1/',
            dcterms: 'http://purl.org/dc/terms/',
            xsi: 'http://www.w3.org/2001/XMLSchema-instance',
            dnx: 'http://www.exlibrisgroup.com/dps/dnx',
            xlin: 'http://www.w3.org/1999/xlink'
    ]

    final String xml
    private GPathResult gPathResult

    SipXmlExtractor(String xml) {
        this.xml = xml
    }

    Sip asSip() {
        Sip sip = new Sip()
        sip.title = extractTitle()

        sip.year = extractYear()
        sip.month = extractMonth()
        sip.dayOfMonth = extractDayOfMonth()
        sip.updateFromDateFields()

        sip.issued = extractIssueNumber()

        sip.ieEntityType = extractIEEntityType()
        sip.objectIdentifierType = extractObjectIdentifierType()
        sip.objectIdentifierValue = extractObjectIdentifierValue()
        sip.policyId = extractPolicyId()

        sip.preservationType = extractPreservationType()
        sip.usageType = extractUsageType()
        sip.digitalOriginal = extractDigitalOriginal()
        sip.revisionNumber = extractRevisionNumber()

        sip.fileWrappers = extractFileWrappers()

        return sip
    }

    List<Sip.FileWrapper> extractFileWrappers() {
        List<Sip.FileWrapper> fileWrappers = [ ]
        boolean moreRecords = true
        int fileIdIndex = 0
        while (moreRecords) {
            fileIdIndex += 1
            GPathResult fileIdRecord = extractFileIdRecord(fileIdIndex)
            if (fileIdRecord == null || (fileIdRecord.children().size() == 0)) {
                moreRecords = false
            } else {
                Sip.FileWrapper sipFileWrapper = new Sip.FileWrapper()
                sipFileWrapper.creationDate = extractFileCreationDate(fileIdRecord)
                sipFileWrapper.fileOriginalName = extractFileOriginalName(fileIdRecord)
                sipFileWrapper.fileOriginalPath = extractFileOriginalPath(fileIdRecord)
                sipFileWrapper.fileSizeBytes = extractFileSizeBytes(fileIdRecord)
                sipFileWrapper.fixityType = extractFileFixityType(fileIdRecord)
                sipFileWrapper.fixityValue = extractFileFixityValue(fileIdRecord)
                sipFileWrapper.label = extractFileLabel(fileIdRecord)
                sipFileWrapper.mimeType = extractFileMimeType(fileIdRecord)
                sipFileWrapper.modificationDate = extractFileModificationDate(fileIdRecord)

                fileWrappers.add(sipFileWrapper)
            }
        }

        return fileWrappers
    }

    GPathResult extractIeDmdRecord() {
        GPathResult gPath = extractGPathResult()
        GPathResult ieDmd = (GPathResult) gPath.'mets:dmdSec'.find { GPathResult childPath ->
            childPath.@ID == "ie-dmd"
        }
        GPathResult ieDmdRecord = ieDmd.'mets:mdWrap'.'mets:xmlData'.'dc:record'
        return ieDmdRecord
    }

    String extractTitle() {
        return extractIeDmdRecord().'dc:title' as String
    }

    Integer extractYear() {
        return Integer.parseInt(extractDcDate())
    }

    String extractDcDate() {
        String dcDateString = extractIeDmdRecord().'dc:date' as String

        return dcDateString
    }

    Integer extractMonth() {
        return Integer.parseInt(extractDcTermsAvailable())
    }

    String extractDcTermsAvailable() {
        String dcTermsAvailableString = extractIeDmdRecord().'dcterms:available' as String

        return dcTermsAvailableString
    }

    Integer extractDayOfMonth() {
        return Integer.parseInt(extractDcCoverage())
    }

    String extractDcCoverage() {
        String dcCoverageString = extractIeDmdRecord().'dc:coverage' as String

        return dcCoverageString
    }

    GPathResult extractIeAmdRecord() {
        GPathResult gPath = extractGPathResult()
        GPathResult ieAmdRecord = (GPathResult) gPath.'mets:amdSec'.find { GPathResult childPath ->
            childPath.@ID == "ie-amd"
        }
        return ieAmdRecord
    }

    GPathResult extractIeAmdTechRecord() {
        GPathResult ieAmdRecord = extractIeAmdRecord()
        GPathResult ieAmdTech = (GPathResult) ieAmdRecord.'mets:techMD'.find { GPathResult childPath ->
            childPath.@ID == "ie-amd-tech"
        }
        return ieAmdTech
    }

    IEEntityType extractIEEntityType() {
        String ieEntityTypeString = extractFirstNodeWithAttribute(extractIeAmdTechRecord(), "id", "IEEntityType") as String

        return IEEntityType.matching(ieEntityTypeString)
    }

    GPathResult extractObjectIdentifier() {
        return extractFirstNodeWithAttribute(extractIeAmdTechRecord(), "id", "objectIdentifier")
    }

    String extractObjectIdentifierType() {
        return extractFirstNodeWithAttribute(extractObjectIdentifier(), "id", "objectIdentifierType") as String
    }

    String extractObjectIdentifierValue() {
        return extractFirstNodeWithAttribute(extractObjectIdentifier(), "id", "objectIdentifierValue") as String
    }

    String extractPolicyId() {
        GPathResult ieAmdRights = extractFirstNodeWithAttribute(extractIeAmdRecord(), "ID", "ie-amd-rights")
        GPathResult accessRightsPolicy = extractFirstNodeWithAttribute(ieAmdRights, "id", "accessRightsPolicy")
        return extractFirstNodeWithAttribute(accessRightsPolicy, "id", "policyId") as String
    }

    GPathResult extractGeneralRepCharacteristicsRecord() {
        GPathResult gPath = extractGPathResult()
        GPathResult rep1Amd = (GPathResult) gPath.'mets:amdSec'.find { GPathResult childPath ->
            childPath.@ID == "rep1-amd"
        }
        println("rep1Amd=${rep1Amd}")
        GPathResult rep1AmdTech = (GPathResult) rep1Amd.'mets:techMD'.find { GPathResult childPath ->
            childPath.@ID == "rep1-amd-tech"
        }
        println("rep1AmdTech=${rep1AmdTech}")
        GPathResult generalRepCharacteristics = (GPathResult) rep1AmdTech."mets:mdWrap"."mets:xmlData"."dnx".section.find { GPathResult childPath ->
            childPath.@id == "generalRepCharacteristics"
        }
        println("generalRepCharacteristics=${generalRepCharacteristics}, class=${generalRepCharacteristics.getClass().getName()}")
        GPathResult generalRepCharacteristicsRecord = generalRepCharacteristics.record
        println("generalRepCharacteristicsRecord=${generalRepCharacteristicsRecord}, class=${generalRepCharacteristicsRecord.getClass().getName()}")
        return generalRepCharacteristicsRecord
    }

    String extractPreservationTypeViaRecord() {
        GPathResult generalRepCharacteristicsRecord = extractGeneralRepCharacteristicsRecord()
        return generalRepCharacteristicsRecord.key.find { GPathResult childPath ->
            childPath.@id == "preservationType"
        }
    }

    String extractPreservationType() {
        return extractFirstNodeWithAttribute("id", "preservationType") as String
     }

    String extractUsageType() {
        return extractFirstNodeWithAttribute("id", "usageType") as String
    }

    String extractIssueNumber() {
        String dcTermsIssuedString = extractIeDmdRecord().'dcterms:issued' as String
        return dcTermsIssuedString != null ? dcTermsIssuedString : null
    }

    Integer extractRevisionNumber() {
        String revisionString = extractFirstNodeWithAttribute("id", "RevisionNumber") as String
        return Integer.parseInt(revisionString)
    }

    Boolean extractDigitalOriginal() {
        String booleanString = extractFirstNodeWithAttribute("id", "DigitalOriginal") as String
        return Boolean.parseBoolean(booleanString)
    }

    GPathResult extractFileIdRecord(int fileIdIndex) {
        GPathResult gPath = extractGPathResult()
        return gPath."mets:amdSec".find { GPathResult childPath ->
            childPath.@ID == "fid${fileIdIndex}-1-amd"
        }
    }

    LocalDateTime extractFileCreationDate(GPathResult fileRecord) {
        String dateString = extractFirstNodeWithAttribute(fileRecord, "id", "fileCreationDate") as String
        return LocalDateTime.parse(dateString, Sip.LOCAL_DATE_TIME_FORMATTER)
    }

    LocalDateTime extractFileModificationDate(GPathResult fileRecord) {
        String dateString = extractFirstNodeWithAttribute(fileRecord, "id", "fileModificationDate") as String
        return LocalDateTime.parse(dateString, Sip.LOCAL_DATE_TIME_FORMATTER)
    }

    String extractFileOriginalPath(GPathResult fileRecord) {
        return extractFirstNodeWithAttribute(fileRecord, "id", "fileOriginalPath") as String
    }

    String extractFileLabel(GPathResult fileRecord) {
        return extractFirstNodeWithAttribute(fileRecord, "id", "label") as String
    }

    String extractFileOriginalName(GPathResult fileRecord) {
        return extractFirstNodeWithAttribute(fileRecord, "id", "fileOriginalName") as String
    }

    Long extractFileSizeBytes(GPathResult fileRecord) {
        String sizeString = extractFirstNodeWithAttribute(fileRecord, "id", "fileSizeBytes") as String
        return Long.parseLong(sizeString)
    }

    String extractFileMimeType(GPathResult fileRecord) {
        return extractFirstNodeWithAttribute(fileRecord, "id", "fileMIMEType") as String
    }

    GPathResult extractFileFixityRecord(GPathResult fileRecord) {
        return extractFirstNodeWithAttribute(fileRecord, "id", "fileFixity")
    }

    String extractFileFixityType(GPathResult fileRecord) {
        return extractFirstNodeWithAttribute(extractFileFixityRecord(fileRecord), "id", "fixityType") as String
    }

    String extractFileFixityValue(GPathResult fileRecord) {
        return extractFirstNodeWithAttribute(extractFileFixityRecord(fileRecord), "id", "fixityValue") as String
    }

    GPathResult extractFirstNodeWithAttribute(String attributeName, String attributeValue) {
        return extractFirstNodeWithAttribute(extractGPathResult(), attributeName, attributeValue)
    }

    GPathResult extractFirstNodeWithAttribute(GPathResult startingPoint, String attributeName, String attributeValue) {
         GPathResult matchingNode = (GPathResult) startingPoint.depthFirst().find { GPathResult child ->
            child.@"${attributeName}" == attributeValue
        }
        return matchingNode
    }

    List<GPathResult> extractNodesWithAttribute(String attributeName, String attributeValue) {
        GPathResult gPath = extractGPathResult()
        List<GPathResult> matchingNodes = gPath.depthFirst().findAll { GPathResult child ->
            child.@"${attributeName}" == attributeValue
        }
        if (matchingNodes.size() > 1) {
            log.warn("TODO Error? Log it? attributeName=${attributeName}, attributeValue=${attributeValue}, matchingNodes=${matchingNodes}")
        }
        return matchingNodes
    }

    private GPathResult extractGPathResult() {
        synchronized (this) {
            if (this.gPathResult == null) {
                XmlSlurper xmlSlurper = new XmlSlurper(false, true)
                this.gPathResult = xmlSlurper.parseText(this.xml)
                this.gPathResult.declareNamespace(SIP_NAMESPACES)
            }
        }
        return this.gPathResult
    }

}
