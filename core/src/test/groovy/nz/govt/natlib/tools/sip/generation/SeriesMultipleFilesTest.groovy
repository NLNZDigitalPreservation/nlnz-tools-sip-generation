package nz.govt.natlib.tools.sip.generation

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

import static org.hamcrest.core.Is.is

import nz.govt.natlib.tools.sip.Sip
import org.apache.commons.io.FileUtils

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import nz.govt.natlib.tools.sip.SipTestHelper
import org.junit.Test

/**
 * Tests the {@code series-sequential} scenario.
 */
class SeriesMultipleFilesTest {
    static final String RESOURCES_FOLDER = "scenario-series-sequential"
    static final String RESOURCES_FOLDER_PARENT = "sip-creation-tests"
    static final Comparator<Path> FILENAME_COMPARATOR = { Path a, Path b ->
        a.fileName.toString() <=> b.fileName.toString()
    }

    @Test
    void correctlyAssembleSipFromFiles() {
        Path parentFolder = SipTestHelper.getFileFromResourceOrFile(RESOURCES_FOLDER, RESOURCES_FOLDER_PARENT)

        assertTrue("Folder=${parentFolder.normalize().toString()} must be a directory containing files for processing", Files.isDirectory(parentFolder))
        if (Files.isDirectory(parentFolder)) {
            List<Path> matchingFiles = getMatchingFiles(parentFolder, ".*?\\.pdf")
            Sip sip = SipTestHelper.sipOneWithoutFiles()

            int fileIndex = 0
            Collections.sort(matchingFiles, FILENAME_COMPARATOR)
            matchingFiles.each { Path file ->
                fileIndex += 1
                Sip.FileWrapper fileWrapper = new Sip.FileWrapper()
                fileWrapper.mimeType = SipTestHelper.SIP_FILE_MIME_TYPE
                fileWrapper.file = file
                fileWrapper.fileOriginalPath = file.fileName.toString()
                fileWrapper.fileOriginalName = file.fileName.toString()
                fileWrapper.label = "${SipTestHelper.SIP_FILE_LABEL_BASE}${fileIndex}"
                fileWrapper.creationDate = SipTestHelper.SIP_FILE_CREATION_DATE
                fileWrapper.modificationDate = SipTestHelper.SIP_FILE_MODIFICATION_DATE
                fileWrapper.fileSizeBytes = Files.size(file)
                fileWrapper.fixityType = SipTestHelper.SIP_FILE_FIXITY_TYPE
                fileWrapper.fixityValue = MD5Generator.calculateMd5Hash(file)

                sip.fileWrappers.add(fileWrapper)
            }

            assertThat("There are 10 files", sip.fileWrappers.size(), is((Integer) 10))

            int fileWrapperIndex = 0
            sip.fileWrappers.each { Sip.FileWrapper fileWrapper ->
                fileWrapperIndex += 1
                validateFileWrapper(fileWrapperIndex, fileWrapper)
            }

            // If you want to see the SIP generated, uncomment this section
            //boolean prettyPrint = true
            //println("sip=${SipJsonGenerator.toJson(sip, prettyPrint)}")
        }
    }

    void validateFileWrapper(int index, Sip.FileWrapper fileWrapper) {
        String fileNumber = new Integer(index).toString().padLeft(3, "0")
        String filename = "TSTPB1-20181123-${fileNumber}.pdf"
        assertThat("fileWrapper.mimeType=${fileWrapper.mimeType} is correct", fileWrapper.mimeType, is(SipTestHelper.SIP_FILE_MIME_TYPE))
        assertNotNull("fileWrapper.file=${fileWrapper.file} not null", fileWrapper.file)

        assertThat("fileWrapper.fileOriginalPath=${fileWrapper.fileOriginalPath}", fileWrapper.fileOriginalPath, is(filename))
        assertThat("fileWrapper.fileOriginalName=${fileWrapper.fileOriginalName}", fileWrapper.fileOriginalName, is(filename))
        assertThat("fileWrapper.label=${fileWrapper.label}", fileWrapper.label, is((String) "${SipTestHelper.SIP_FILE_LABEL_BASE}${index}"))
        assertThat("fileWrapper.creationDate=${fileWrapper.creationDate}", fileWrapper.creationDate, is(SipTestHelper.SIP_FILE_CREATION_DATE))
        assertThat("fileWrapper.modificationDate=${fileWrapper.modificationDate}", fileWrapper.modificationDate, is(SipTestHelper.SIP_FILE_MODIFICATION_DATE))
        assertTrue("fileWrapper.fileSizeBytes=${fileWrapper.fileSizeBytes}", fileWrapper.fileSizeBytes > 0L)
        assertThat("fileWrapper.fixityType=${fileWrapper.fixityType}", fileWrapper.fixityType, is(SipTestHelper.SIP_FILE_FIXITY_TYPE))
        assertNotNull("fileWrapper.fixityValue={fileWrapper.fixityValue}", fileWrapper.fixityValue)
    }

    List<Path> getMatchingFiles(Path parentFolder, String pattern) {
        List<File> foundFiles = FileUtils.listFiles(parentFolder.toFile(), null, true)
        List<Path> foundPaths = [ ]
        foundFiles.each { File file ->
            if (file.absolutePath ==~ /${pattern}/) {
                foundPaths.add(file.toPath())
            }
        }
        return foundPaths
    }
}
