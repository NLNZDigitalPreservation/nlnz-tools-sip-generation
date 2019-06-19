package nz.govt.natlib.tools.sip.pdf.thumbnail

import nz.govt.natlib.tools.sip.state.SipProcessingException
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.nio.file.Files

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

// We ignore the whole class as there's no guarantee that pdftoppm is installed on the test system.
// Ensure that this file is checked in with the @Ignore NOT commented out.
@Ignore
class CommandLinePdfToThumbnailFileGeneratorTest {
    static final String TEST_FILE_RESOURCES_PATH = "src/test/resources/nz/govt/natlib/tools/sip/pdf/thumbnail"

    static final String SINGLE_A3_WIDE_PAGE_PDF_NAME = "sample-a3-wide-pdf-with-some-images.pdf"
    static final String SINGLE_A4_PAGE_PDF_NAME = "sample-a4-pdf-with-an-image.pdf"
    static final String MULTIPLE_A4_PAGE_PDF_NAME = "sample-multi-page-a4-pdf-with-images.pdf"

    static final String EXPECTED_FILE_THUMBNAIL_SINGLE_A4_NAME_PNG = "sample-a4-pdf-with-an-image_thumbnail_pdftoppm.png"
    static final String EXPECTED_FILE_THUMBNAIL_SINGLE_A4_NAME_JPEG = "sample-a4-pdf-with-an-image_thumbnail_pdftoppm_40q.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_WIDE_A3_NAME_PNG = "sample-a3-wide-pdf-with-some-images_thumbnail_pdftoppm.png"
    static final String EXPECTED_FILE_THUMBNAIL_WIDE_A3_NAME_JPEG = "sample-a3-wide-pdf-with-some-images_thumbnail_pdftoppm_40q.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_NAME_PNG = "sample-multi-page-a4-pdf-with-images_0_pdftoppm.png"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_NAME_JPEG = "sample-multi-page-a4-pdf-with-images_0_pdftoppm_40q.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_NAME_PNG = "sample-multi-page-a4-pdf-with-images_1_pdftoppm.png"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_NAME_JPEG = "sample-multi-page-a4-pdf-with-images_1_pdftoppm_40q.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_NAME_PNG = "sample-multi-page-a4-pdf-with-images_2_pdftoppm.png"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_NAME_JPEG = "sample-multi-page-a4-pdf-with-images_2_pdftoppm_40q.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_INVALID_PDF_NAME_PNG = "sample-invalid-pdf_thumbnail.png"

    static File WORKING_DIRECTORY
    static File RESOURCES_DIRECTORY
    static File PDF_FILE_SINGLE_A4
    static File EXPECTED_FILE_THUMBNAIL_SINGLE_A4_PNG
    static File EXPECTED_FILE_THUMBNAIL_SINGLE_A4_JPEG
    static File PDF_FILE_WIDE_A3
    static File EXPECTED_FILE_THUMBNAIL_WIDE_A3_PNG
    static File EXPECTED_FILE_THUMBNAIL_WIDE_A3_JPEG
    static File PDF_FILE_MULTIPLE_A4
    static File EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_PNG
    static File EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_JPEG
    static File EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_PNG
    static File EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_JPEG
    static File EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_PNG
    static File EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_JPEG
    static File EXPECTED_FILE_INVALID_PDF_THUMBNAIL_PNG

    static ThumbnailParameters DEFAULT_PARAMETERS

    List<File> filesToDelete

    @BeforeClass
    static void setupStatic() {
        WORKING_DIRECTORY = new File(System.getProperty("user.dir"))
        RESOURCES_DIRECTORY = new File(WORKING_DIRECTORY, TEST_FILE_RESOURCES_PATH)

        PDF_FILE_SINGLE_A4 = new File(RESOURCES_DIRECTORY, SINGLE_A4_PAGE_PDF_NAME)
        PDF_FILE_WIDE_A3 = new File(RESOURCES_DIRECTORY, SINGLE_A3_WIDE_PAGE_PDF_NAME)
        PDF_FILE_MULTIPLE_A4 = new File(RESOURCES_DIRECTORY, MULTIPLE_A4_PAGE_PDF_NAME)

        EXPECTED_FILE_THUMBNAIL_SINGLE_A4_PNG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_SINGLE_A4_NAME_PNG)
        EXPECTED_FILE_THUMBNAIL_SINGLE_A4_JPEG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_SINGLE_A4_NAME_JPEG)
        EXPECTED_FILE_THUMBNAIL_WIDE_A3_PNG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_WIDE_A3_NAME_PNG)
        EXPECTED_FILE_THUMBNAIL_WIDE_A3_JPEG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_WIDE_A3_NAME_JPEG)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_PNG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_NAME_PNG)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_JPEG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_NAME_JPEG)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_PNG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_NAME_PNG)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_JPEG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_NAME_JPEG)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_PNG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_NAME_PNG)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_JPEG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_NAME_JPEG)
        EXPECTED_FILE_INVALID_PDF_THUMBNAIL_PNG = new File(RESOURCES_DIRECTORY, EXPECTED_FILE_THUMBNAIL_INVALID_PDF_NAME_PNG)

        DEFAULT_PARAMETERS = new ThumbnailParameters(thumbnailHeight: 250, textHeight: 20, quality: 40)
    }

    @Before
    void setup() {
        filesToDelete = [ ]
    }

    @After
    void tearDown() {
        // Set to true if you want to keep the created files.
        boolean deleteCreatedFiles = true
        if (deleteCreatedFiles) {
            filesToDelete.each { File fileToDelete ->
                if (fileToDelete.exists()) {
                    fileToDelete.delete()
                }
            }
        }
    }

    @Test
    void correctIdentifiesFileTypes() {
        File testFile = new File("abc.jpg")
        assertTrue("file=${testFile.name} is jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertFalse("file=${testFile.name} is not png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("abc.jpeg")
        assertTrue("file=${testFile.name} is jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertFalse("file=${testFile.name} is not png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("abc.JPG")
        assertTrue("file=${testFile.name} is jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertFalse("file=${testFile.name} is not png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("abc.Jpeg")
        assertTrue("file=${testFile.name} is jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertFalse("file=${testFile.name} is not png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("abc.JPEG")
        assertTrue("file=${testFile.name} is jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertFalse("file=${testFile.name} is not png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("abc.png")
        assertFalse("file=${testFile.name} is not jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertTrue("file=${testFile.name} is png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("abc.PNG")
        assertFalse("file=${testFile.name} is not jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertTrue("file=${testFile.name} is png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("abc.pNg")
        assertFalse("file=${testFile.name} is not jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertTrue("file=${testFile.name} is png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))

        testFile = new File("this-is-not-an-image.file")
        assertFalse("file=${testFile.name} is not jpeg", CommandLinePdfToThumbnailFileGenerator.isJpegFilename(testFile.name))
        assertFalse("file=${testFile.name} not png", CommandLinePdfToThumbnailFileGenerator.isPngFilename(testFile.name))
    }

    @Test
    void correctConvertsPdfToJpeg() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_WIDE_A3, ".jpeg",
                [ EXPECTED_FILE_THUMBNAIL_WIDE_A3_JPEG ], parameters, true)

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_SINGLE_A4, ".jpeg",
                [ EXPECTED_FILE_THUMBNAIL_SINGLE_A4_JPEG ], parameters, true)

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_MULTIPLE_A4, ".jpeg",
                [ EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_JPEG, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_JPEG,
                  EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_JPEG ],
                parameters, true)
    }

    @Test
    void correctConvertsPdfToPng() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_WIDE_A3, ".png",
                [ EXPECTED_FILE_THUMBNAIL_WIDE_A3_PNG ], parameters, true)

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_SINGLE_A4, ".png",
                [ EXPECTED_FILE_THUMBNAIL_SINGLE_A4_PNG ], parameters, true)

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_MULTIPLE_A4, ".png",
                [ EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_PNG, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_PNG,
                  EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_PNG ],
                parameters, true)
    }

    @Test(expected =  SipProcessingException.class)
    void correctlyThrowsSipProcessingExceptionWhenNonexistentDirectory() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        File nonexistantFile = new File("this-file-does-not-exist.anywhere")
        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(nonexistantFile, nonexistantFile.parentFile,
                "there-is-no-file-here", ".png", parameters, true, true)
        assertTrue("This point should not be reached", false)
    }

    @Test(expected =  SipProcessingException.class)
    void correctlyThrowsSipProcessingExceptionWhenNonexistentFile() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        File nonexistantFile = new File(FileUtils.tempDirectory,"this-file-does-not-exist.anywhere")
        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(nonexistantFile, nonexistantFile.parentFile,
                "there-is-no-file-here", ".png", parameters, true, true)
        assertTrue("This point should not be reached", false)
    }

    void verifyGeneratorCreatesExpectedThumbnails(File pdfFile, String suffix, List<File> expectedThumbnails,
                                                        ThumbnailParameters parameters, boolean hasCaption) {
        File tempDirectory = Files.createTempDirectory(FileUtils.tempDirectory.toPath(),
                "CommandLinePdfToThumbnailFileGeneratorTest_").toFile()
        tempDirectory.deleteOnExit()

        List<File> thumbnailFiles = CommandLinePdfToThumbnailFileGenerator.generateThumbnails(pdfFile,
                tempDirectory, "test-thumbnail", suffix, parameters, hasCaption)
        assertThat("Expect numberThumbnails=${expectedThumbnails.size()}", thumbnailFiles.size(),
                is(expectedThumbnails.size()))

        filesToDelete.addAll(thumbnailFiles)

        thumbnailFiles.eachWithIndex{ File thumbnailFile, int index ->
            BufferedImage expectedThumbnailImage = loadImage(expectedThumbnails.get(index))
            BufferedImage thumbnailImage = loadImage(thumbnailFile)
            assertTrue("Generated thumbnail=${thumbnailFile.getName()} matches " +
                    "expected=${expectedThumbnails.get(index).getName()} for pdfFile=${pdfFile.name}",
                    bufferedImagesAreIdentical(expectedThumbnailImage, thumbnailImage))
        }
    }

    static BufferedImage loadImage(File imageFile) {
        BufferedImage bufferedImage = ImageIO.read(imageFile)
        return bufferedImage
    }

    static boolean bufferedImagesAreIdentical(BufferedImage image1, BufferedImage image2) {
        DataBuffer dataBuffer1 = image1.getData().getDataBuffer()
        DataBuffer dataBuffer2 = image2.getData().getDataBuffer()
        if (dataBuffer1.getSize() != dataBuffer2.getSize()) {
            println("TEST ERROR data buffers are different sizes, image1 size=${dataBuffer1.getSize()}, image2 size=${dataBuffer2.getSize()}")
            return false
        }
        for (int index = 0; index < dataBuffer1.getSize(); index++) {
            if (dataBuffer1.getElem(index) != dataBuffer2.getElem(index)) {
                println("TEST ERROR data buffers element mismatch at index=${index}")
                return false
            }
        }
        return true
    }

    // This method generates the given PDF thumbnails. If the algorithm or layout/format changes, then regenerate the
    // test thumbnails and check them in as required.
    // The generated thumbnails will appear in the System temp directory.
    @Test
    @Ignore
    void generateTestPdfThumbnails() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()
        File tempDirectory = FileUtils.tempDirectory

        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(PDF_FILE_SINGLE_A4, tempDirectory,
                "THUMBNAIL_SINGLE_A4_pdftoppm", ".png", parameters, true, false)
        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(PDF_FILE_SINGLE_A4, tempDirectory,
                "THUMBNAIL_SINGLE_A4_pdftoppm", ".jpeg", parameters, true, false)

        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(PDF_FILE_WIDE_A3, tempDirectory,
                "THUMBNAIL_WIDE_A3_pdftoppm", ".png", parameters, true, false)
        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(PDF_FILE_WIDE_A3, tempDirectory,
                "THUMBNAIL_WIDE_A3_pdftoppm", ".jpeg", parameters, true, false)

        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(PDF_FILE_MULTIPLE_A4, tempDirectory,
                "THUMBNAIL_MULTIPLE_A4_pdftoppm", ".png", parameters, true, false)
        CommandLinePdfToThumbnailFileGenerator.generateThumbnails(PDF_FILE_MULTIPLE_A4, tempDirectory,
                "THUMBNAIL_MULTIPLE_A4_pdftoppm", ".jpeg", parameters, true, false)
    }


}
