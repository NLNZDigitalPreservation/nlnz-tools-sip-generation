package nz.govt.natlib.tools.sip.generation

import com.exlibris.core.sdk.consts.Enum
import com.exlibris.core.sdk.formatting.DublinCore
import com.exlibris.digitool.common.dnx.DnxDocument
import com.exlibris.digitool.common.dnx.DnxDocumentHelper
import com.exlibris.dps.sdk.deposit.IEParser
import com.exlibris.dps.sdk.deposit.IEParserFactory
import gov.loc.mets.FileType
import gov.loc.mets.MetsType
import nz.govt.natlib.tools.sip.Sip

class SipXmlGenerator {
    private Sip sip
    private IEParser ieParser
    private Boolean sipGenerated = false

    SipXmlGenerator(Sip sip) {
        this.sip = sip
        this.ieParser = IEParserFactory.create()
    }

    void generateSip() {
        synchronized (this) {
            DublinCore dublinCore = ieParser.getDublinCoreParser()
            // National Library does not set dc:creator in their SIPs
            //dublinCore.addElement("dc:creator", getValue("dc:creator", sipParameters, parameterOverrides))
            // National Library does not set dc:identifier in their SIPs
            //dublinCore.addElement("dc:identifier", getValue("dc:identifier", sipParameters, ))

            dublinCore.addElement("dc:title", sip.title)
            dublinCore.addElement("dc:date", sip.year.toString())
            dublinCore.addElement("dcterms:available", sip.month.toString())
            dublinCore.addElement("dc:coverage", sip.dayOfMonth.toString())

            ieParser.setIEDublinCore(dublinCore)

            // IE level DNX construction
            DnxDocument dnxDocument = ieParser.getDnxParser()
            DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument)

            DnxDocumentHelper.GeneralIECharacteristics generalIECharacteristics = new DnxDocumentHelper.GeneralIECharacteristics(documentHelper)
            generalIECharacteristics.setIEEntityType(sip.ieEntityType.getFieldValue())
            documentHelper.setGeneralIECharacteristics(generalIECharacteristics)

            DnxDocumentHelper.ObjectIdentifier objectIdentifier = new DnxDocumentHelper.ObjectIdentifier(documentHelper)
            objectIdentifier.setObjectIdentifierType(sip.objectIdentifierType)
            objectIdentifier.setObjectIdentifierValue(sip.objectIdentifierValue)
            documentHelper.setObjectIdentifiers([ objectIdentifier ])

            DnxDocumentHelper.AccessRightsPolicy accessRightsPolicy = new DnxDocumentHelper.AccessRightsPolicy(documentHelper)
            accessRightsPolicy.setPolicyId(sip.policyId)
            documentHelper.setAccessRightsPolicy(accessRightsPolicy)

            // In order for document construction to work correctly, the dnxDocument must be added AFTER it has been populated
            ieParser.setIeDnx(dnxDocument)

            addFiles()

            ieParser.generateStructMap(null, null, "Table of Contents")

            sipGenerated = true
        }
    }

    String getSipAsXml() {
        if (!sipGenerated) {
            generateSip()
        }
        return ieParser.toXML()
    }

    IEParser getIEParser() {
        return this.ieParser
    }

    void addFiles() {
        // add fileGrp
        MetsType.FileSec.FileGrp fileGroup = ieParser.addNewFileGrp(Enum.UsageType.VIEW,
                Enum.PreservationType.PRESERVATION_MASTER)

        // File group level DNX construction
        DnxDocument dnxDocument = ieParser.getFileGrpDnx(fileGroup.getID())
        DnxDocumentHelper documentHelper = new DnxDocumentHelper(dnxDocument)

        DnxDocumentHelper.GeneralRepCharacteristics generalRepCharacteristics = documentHelper.getGeneralRepCharacteristics()
        generalRepCharacteristics.setPreservationType(sip.preservationType)
        generalRepCharacteristics.setUsageType(sip.usageType)
        generalRepCharacteristics.setDigitalOriginal(sip.digitalOriginal.toString())
        generalRepCharacteristics.setRevisionNumber(sip.revisionNumber.toString())

        ieParser.setFileGrpDnx(documentHelper.getDocument(), fileGroup.getID())

        sip.fileWrappers.each { Sip.FileWrapper fileWrapper ->
            // Add file and DNX metadata on file
            String mimeType = fileWrapper.getMimeType()
            File file = fileWrapper.getFile()
            FileType fileType = ieParser.addNewFile(fileGroup, mimeType, fileWrapper.getFileOriginalName(), "XXX-TO-REPLACE-test file")

            // File DNX construction
            DnxDocument dnxDocumentFile = ieParser.getFileDnx(fileType.getID())
            DnxDocumentHelper fileDocumentHelper = new DnxDocumentHelper(dnxDocumentFile)
            fileDocumentHelper.getGeneralFileCharacteristics().setFileCreationDate(Sip.LOCAL_DATE_TIME_FORMATTER.format(fileWrapper.creationDate))
            fileDocumentHelper.getGeneralFileCharacteristics().setFileModificationDate(Sip.LOCAL_DATE_TIME_FORMATTER.format(fileWrapper.modificationDate))
            fileDocumentHelper.getGeneralFileCharacteristics().setFileOriginalName(fileWrapper.fileOriginalName)
            fileDocumentHelper.getGeneralFileCharacteristics().setFileOriginalPath(fileWrapper.fileOriginalPath)
            fileDocumentHelper.getGeneralFileCharacteristics().setFileSizeBytes(fileWrapper.fileSizeBytes.toString())
            fileDocumentHelper.getGeneralFileCharacteristics().setLabel(fileWrapper.label)

            DnxDocumentHelper.FileFixity fileFixity = new DnxDocumentHelper.FileFixity(documentHelper)
            fileFixity.setFixityType(fileWrapper.fixityType)
            fileFixity.setFixityValue(fileWrapper.fixityValue)
            fileDocumentHelper.setFileFixitys([ fileFixity ])

            // In order for document construction to work correctly, the dnxDocument must be added AFTER it has been populated
            ieParser.setFileDnx(fileDocumentHelper.getDocument(), fileType.getID())
        }

        // ieParser can generate checksums as well
        // TODO Make checksum generation optional?
        //String filesRootFolder = fileWrappers.first().file.parentFile.getCanonicalPath()
        //ieParser.generateChecksum(filesRootFolder, Enum.FixityType.MD5.toString())
        //ieParser.updateSize(filesRootFolder)
    }
}
