package nz.govt.natlib.tools.sip.extraction

import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import nz.govt.natlib.tools.sip.Sip

import java.time.LocalDateTime

@Slf4j
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
        sip.title = getTitle()

        sip.year = getYear()
        sip.month = getMonth()
        sip.dayOfMonth = getDayOfMonth()

        sip.ieEntityType = getIEEntityType()
        sip.objectIdentifierType = getObjectIdentifierType()
        sip.objectIdentifierValue = getObjectIdentifierValue()
        sip.policyId = getPolicyId()

        sip.preservationType = getPreservationType()
        sip.usageType = getUsageType()
        sip.digitalOriginal = getDigitalOriginal()
        sip.revisionNumber = getRevisionNumber()

        sip.fileWrappers = getFileWrappers()

        return sip
    }

    List<Sip.FileWrapper> getFileWrappers() {
        List<Sip.FileWrapper> fileWrappers = [ ]
        boolean moreRecords = true
        int fileIdIndex = 0
        while (moreRecords) {
            fileIdIndex += 1
            GPathResult fileIdRecord = getFileIdRecord(fileIdIndex)
            if (fileIdRecord == null || (fileIdRecord.children().size() == 0)) {
                moreRecords = false
            } else {
                Sip.FileWrapper sipFileWrapper = new Sip.FileWrapper()
                sipFileWrapper.creationDate = getFileCreationDate(fileIdRecord)
                sipFileWrapper.fileOriginalName = getFileOriginalName(fileIdRecord)
                sipFileWrapper.fileOriginalPath = getFileOriginalPath(fileIdRecord)
                sipFileWrapper.fileSizeBytes = getFileSizeBytes(fileIdRecord)
                sipFileWrapper.fixityType = getFileFixityType(fileIdRecord)
                sipFileWrapper.fixityValue = getFileFixityValue(fileIdRecord)
                sipFileWrapper.label = getFileLabel(fileIdRecord)
                sipFileWrapper.mimeType = getFileMimeType(fileIdRecord)
                sipFileWrapper.modificationDate = getFileModificationDate(fileIdRecord)

                fileWrappers.add(sipFileWrapper)
            }
        }

        return fileWrappers
    }

    GPathResult getIeDmdRecord() {
        GPathResult gPath = getGPathResult()
        GPathResult ieDmd = (GPathResult) gPath.'mets:dmdSec'.find { GPathResult childPath ->
            childPath.@ID == "ie-dmd"
        }
        GPathResult ieDmdRecord = ieDmd.'mets:mdWrap'.'mets:xmlData'.'dc:record'
        return ieDmdRecord
    }

    String getTitle() {
        return getIeDmdRecord().'dc:title' as String
    }

    Integer getYear() {
        String yearString = getIeDmdRecord().'dc:date' as String
        return Integer.parseInt(yearString)
    }

    Integer getMonth() {
        String monthString = getIeDmdRecord().'dcterms:available' as String
        return Integer.parseInt(monthString)
    }

    Integer getDayOfMonth() {
        String dayOfMonthString = getIeDmdRecord().'dc:coverage' as String
        return Integer.parseInt(dayOfMonthString)
    }

    GPathResult getIeAmdRecord() {
        GPathResult gPath = getGPathResult()
        GPathResult ieAmdRecord = (GPathResult) gPath.'mets:amdSec'.find { GPathResult childPath ->
            childPath.@ID == "ie-amd"
        }
        return ieAmdRecord
    }

    GPathResult getIeAmdTechRecord() {
        GPathResult ieAmdRecord = getIeAmdRecord()
        GPathResult ieAmdTech = (GPathResult) ieAmdRecord.'mets:techMD'.find { GPathResult childPath ->
            childPath.@ID == "ie-amd-tech"
        }
        return ieAmdTech
    }

    String getIEEntityType() {
        return findFirstNodeWithAttribute(getIeAmdTechRecord(), "id", "IEEntityType") as String
    }

    GPathResult getObjectIdentifier() {
        return findFirstNodeWithAttribute(getIeAmdTechRecord(), "id", "objectIdentifier")
    }

    String getObjectIdentifierType() {
        return findFirstNodeWithAttribute(getObjectIdentifier(), "id", "objectIdentifierType") as String
    }

    String getObjectIdentifierValue() {
        return findFirstNodeWithAttribute(getObjectIdentifier(), "id", "objectIdentifierValue") as String
    }

    String getPolicyId() {
        GPathResult ieAmdRights = findFirstNodeWithAttribute(getIeAmdRecord(), "ID", "ie-amd-rights")
        GPathResult accessRightsPolicy = findFirstNodeWithAttribute(ieAmdRights, "id", "accessRightsPolicy")
        return findFirstNodeWithAttribute(accessRightsPolicy, "id", "policyId") as String
    }

    GPathResult getGeneralRepCharacteristicsRecord() {
        GPathResult gPath = getGPathResult()
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

    String getPreservationTypeViaRecord() {
        GPathResult generalRepCharacteristicsRecord = getGeneralRepCharacteristicsRecord()
        return generalRepCharacteristicsRecord.key.find { GPathResult childPath ->
            childPath.@id == "preservationType"
        }
    }

    String getPreservationType() {
        return findFirstNodeWithAttribute("id", "preservationType") as String
     }

    String getUsageType() {
        return findFirstNodeWithAttribute("id", "usageType") as String
    }

    Integer getRevisionNumber() {
        String revisionString = findFirstNodeWithAttribute("id", "RevisionNumber") as String
        return Integer.parseInt(revisionString)
    }

    Boolean getDigitalOriginal() {
        String booleanString = findFirstNodeWithAttribute("id", "DigitalOriginal") as String
        return Boolean.parseBoolean(booleanString)
    }

    GPathResult getFileIdRecord(int fileIdIndex) {
        GPathResult gPath = getGPathResult()
        return gPath."mets:amdSec".find { GPathResult childPath ->
            childPath.@ID == "fid${fileIdIndex}-1-amd"
        }
    }

    LocalDateTime getFileCreationDate(GPathResult fileRecord) {
        String dateString = findFirstNodeWithAttribute(fileRecord, "id", "fileCreationDate") as String
        return LocalDateTime.parse(dateString, Sip.LOCAL_DATE_TIME_FORMATTER)
    }

    LocalDateTime getFileModificationDate(GPathResult fileRecord) {
        String dateString = findFirstNodeWithAttribute(fileRecord, "id", "fileModificationDate") as String
        return LocalDateTime.parse(dateString, Sip.LOCAL_DATE_TIME_FORMATTER)
    }

    String getFileOriginalPath(GPathResult fileRecord) {
        return findFirstNodeWithAttribute(fileRecord, "id", "fileOriginalPath") as String
    }

    String getFileLabel(GPathResult fileRecord) {
        return findFirstNodeWithAttribute(fileRecord, "id", "label") as String
    }

    String getFileOriginalName(GPathResult fileRecord) {
        return findFirstNodeWithAttribute(fileRecord, "id", "fileOriginalName") as String
    }

    Long getFileSizeBytes(GPathResult fileRecord) {
        String sizeString = findFirstNodeWithAttribute(fileRecord, "id", "fileSizeBytes") as String
        return Long.parseLong(sizeString)
    }

    String getFileMimeType(GPathResult fileRecord) {
        return findFirstNodeWithAttribute(fileRecord, "id", "fileMIMEType") as String
    }

    GPathResult getFileFixityRecord(GPathResult fileRecord) {
        return findFirstNodeWithAttribute(fileRecord, "id", "fileFixity")
    }

    String getFileFixityType(GPathResult fileRecord) {
        return findFirstNodeWithAttribute(getFileFixityRecord(fileRecord), "id", "fixityType") as String
    }

    String getFileFixityValue(GPathResult fileRecord) {
        return findFirstNodeWithAttribute(getFileFixityRecord(fileRecord), "id", "fixityValue") as String
    }

    GPathResult findFirstNodeWithAttribute(String attributeName, String attributeValue) {
        return findFirstNodeWithAttribute(getGPathResult(), attributeName, attributeValue)
    }

    GPathResult findFirstNodeWithAttribute(GPathResult startingPoint, String attributeName, String attributeValue) {
         GPathResult matchingNode = (GPathResult) startingPoint.depthFirst().find { GPathResult child ->
            child.@"${attributeName}" == attributeValue
        }
        return matchingNode
    }

    List<GPathResult> findNodesWithAttribute(String attributeName, String attributeValue) {
        GPathResult gPath = getGPathResult()
        List<GPathResult> matchingNodes = gPath.depthFirst().findAll { GPathResult child ->
            child.@"${attributeName}" == attributeValue
        }
        if (matchingNodes.size() > 1) {
            log.warn("TODO Error? Log it? attributeName=${attributeName}, attributeValue=${attributeValue}, matchingNodes=${matchingNodes}")
        }
        return matchingNodes
    }

    private GPathResult getGPathResult() {
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
