package nz.govt.natlib.tools.sip.pdf.thumbnail

import groovy.util.logging.Log4j2
import org.apache.commons.io.FilenameUtils
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import java.awt.image.DataBuffer
import java.nio.file.Path

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

@Log4j2
// This test can be an unreliable test depending on the platform where the PDFs are being generated.
// TODO Fix up this test so it runs more reliably.
//@Ignore
class ThumbnailGeneratorTest {
    static final String TEST_FILE_RESOURCES_PATH = FilenameUtils.separatorsToSystem("src/test/resources/nz/govt/natlib/tools/sip/pdf/thumbnail")

    static final String SINGLE_A3_WIDE_PAGE_PDF_NAME = "sample-a3-wide-pdf-with-some-images.pdf"
    static final String SINGLE_A4_PAGE_PDF_NAME = "sample-a4-pdf-with-an-image.pdf"
    static final String MULTIPLE_A4_PAGE_PDF_NAME = "sample-multi-page-a4-pdf-with-images.pdf"
    static final String SAMPLE_INVALID_PDF_NAME = "sample-invalid-pdf.pdf"

    static List<Path> PDF_FILES_LIST_1 = [ ]

    static final String EXPECTED_FILE_THUMBNAIL_SINGLE_A4_NAME = "sample-a4-pdf-with-an-image_thumbnail.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_WIDE_A3_NAME = "sample-a3-wide-pdf-with-some-images_thumbnail.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_NAME = "sample-multi-page-a4-pdf-with-images_0.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_NAME = "sample-multi-page-a4-pdf-with-images_1.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_NAME = "sample-multi-page-a4-pdf-with-images_2.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_INVALID_PDF_NAME = "sample-invalid-pdf_thumbnail.jpeg"
    static final String EXPECTED_FILE_THUMBNAIL_PAGE_NAME = "page-of-thumbnails-from-12-pdf-files-01.jpeg"
    static final String EXPECTED_FILE_PDFTOPPM_THUMBNAIL_PAGE_NAME = "page-of-thumbnails-from-12-pdf-files-pdftoppm-01.jpeg"

    static Path WORKING_DIRECTORY
    static Path RESOURCES_DIRECTORY
    static Path PDF_FILE_SINGLE_A4
    static Path EXPECTED_FILE_THUMBNAIL_SINGLE_A4
    static Path PDF_FILE_WIDE_A3
    static Path EXPECTED_FILE_THUMBNAIL_WIDE_A3
    static Path PDF_FILE_MULTIPLE_A4
    static Path EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0
    static Path EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1
    static Path EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2
    static Path PDF_FILE_INVALID_PDF
    static Path EXPECTED_FILE_INVALID_PDF_THUMBNAIL
    static Path EXPECTED_FILE_THUMBNAIL_PAGE
    static Path EXPECTED_FILE_PDFTOPPM_THUMBNAIL_PAGE

    static ThumbnailParameters DEFAULT_PARAMETERS

    static void generatePdfThumbnails(Path pdfFile, ThumbnailParameters parameters) {
        List< BufferedImage> bufferedImages = ThumbnailGenerator.generateImagesFromPdf(pdfFile, parameters)
        String namePrefix = "MULTIPLE_A4_PAGE_PDF_NAME_"
        bufferedImages.eachWithIndex { BufferedImage bufferedImage, int index ->
            String filenamePrefix = "${namePrefix}_${index}_"
            File thumbnailFile = File.createTempFile(filenamePrefix, ".jpeg")
            thumbnailFile.deleteOnExit()
            ThumbnailGenerator.writeImage(bufferedImage, thumbnailFile.getCanonicalPath(), parameters.dpi)
        }
    }

    static void generatePdfThumbnail(Path pdfFile, String filenamePrefix, ThumbnailParameters parameters) {
        File thumbnailFile = File.createTempFile(filenamePrefix, ".jpeg").toPath()
        thumbnailFile.deleteOnExit()

        ThumbnailGenerator.writeThumbnail(pdfFile, parameters, thumbnailFile.toPath())
    }

    static void verifyGeneratorCreatesExpectedThumbnails(Path sourcePdfFile, List<Path> expectedJpegs,
                                                         ThumbnailParameters parameters) {
        List<BufferedImage> expectedThumbnails = expectedJpegs.collect { Path jpegFile ->
            loadJpeg(jpegFile)
        }

        // While we could use buffered images directly, if it doesn't work, we want to be able to open up that physical
        // file and compare.
        List<BufferedImage> generatedThumbnails = ThumbnailGenerator.generateImagesFromPdf(sourcePdfFile, parameters)
        List<Path> generatedThumbnailFiles = [ ]
        generatedThumbnails.eachWithIndex { BufferedImage thumbnailImage, int index ->
            String indexString = generatedThumbnails.size() > 1 ? "${index}_" : ""
            String filenamePrefix = "${sourcePdfFile.fileName.toString()}_GENERATED_THUMBNAIL_${indexString}"
            Path thumbnailFile = File.createTempFile(filenamePrefix, ".jpeg").toPath()
            // Comment out the deleteOnExit if you want to examine the contents of the file
            thumbnailFile.toFile().deleteOnExit()
            generatedThumbnailFiles.add(thumbnailFile)
            writeImage(thumbnailImage, thumbnailFile.normalize().toString(), parameters.dpi)
        }
        // Note that you can't compare a generated BufferedImage with a JPEG read from a file -- you need to write the
        // BufferedImage to a file and re-read it back in so that it compares same to same
        List<BufferedImage> thumbnailsFromFile = generatedThumbnailFiles.collect { Path thumbnailJpegFile ->
            loadJpeg(thumbnailJpegFile)
        }
        assertThat("Expected file=${sourcePdfFile} to generate total=${generatedThumbnails.size()}, actual=${expectedThumbnails.size()}",
                generatedThumbnails.size(), is(expectedThumbnails.size()))

        thumbnailsFromFile.eachWithIndex { BufferedImage bufferedImage, int index ->
            assertTrue("Generated thumbnail=${generatedThumbnailFiles.get(index).fileName} matches expected=${expectedJpegs.get(index).fileName} for pdf file=${sourcePdfFile}, index=${index}",
                    bufferedImagesAreIdentical(bufferedImage, expectedThumbnails.get(index)))
        }
    }

    static void verifyGeneratorCreatesExpectedThumbnailPage(List<Path> sourcePdfFiles, Path expectedJpegFile,
                                                         ThumbnailParameters parameters) {
        // While we could use buffered images directly, if it doesn't work, we want to be able to open up that physical
        // file and compare.
        String filenamePrefix = "GENERATED_THUMBNAIL_PAGE_"
        Path thumbnailPageFile = File.createTempFile(filenamePrefix, ".jpeg").toPath()
        // Comment out the deleteOnExit if you want to examine the contents of the file
        thumbnailPageFile.toFile().deleteOnExit()

        ThumbnailGenerator.writeThumbnailPage(sourcePdfFiles, parameters, thumbnailPageFile)

        BufferedImage expectedThumbnailPage = loadJpeg(expectedJpegFile)
        BufferedImage thumbnailPage = loadJpeg(thumbnailPageFile)
        assertTrue("Generated thumbnail=${thumbnailPageFile.fileName} matches expected=${expectedJpegFile.fileName} for pdf files=${sourcePdfFiles}",
                bufferedImagesAreIdentical(expectedThumbnailPage, thumbnailPage))
    }

    static BufferedImage loadJpeg(Path jpegFile) {
        BufferedImage bufferedImage = ImageIO.read(jpegFile.toFile())
        return bufferedImage
    }

    static void writeImage(BufferedImage bufferedImage, String path, int dpi) {
        ImageIOUtil.writeImage(bufferedImage, path, dpi)
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

    @BeforeClass
    static void setupStatic() {
        WORKING_DIRECTORY = Path.of(System.getProperty("user.dir"))
        RESOURCES_DIRECTORY = WORKING_DIRECTORY.resolve(TEST_FILE_RESOURCES_PATH)

        PDF_FILE_SINGLE_A4 = RESOURCES_DIRECTORY.resolve(SINGLE_A4_PAGE_PDF_NAME)
        PDF_FILE_WIDE_A3 = RESOURCES_DIRECTORY.resolve(SINGLE_A3_WIDE_PAGE_PDF_NAME)
        PDF_FILE_MULTIPLE_A4 = RESOURCES_DIRECTORY.resolve(MULTIPLE_A4_PAGE_PDF_NAME)
        PDF_FILE_INVALID_PDF = RESOURCES_DIRECTORY.resolve(SAMPLE_INVALID_PDF_NAME)

        PDF_FILES_LIST_1 = [ PDF_FILE_WIDE_A3, PDF_FILE_MULTIPLE_A4, PDF_FILE_INVALID_PDF,
                             PDF_FILE_SINGLE_A4, PDF_FILE_MULTIPLE_A4, PDF_FILE_SINGLE_A4, PDF_FILE_MULTIPLE_A4,
                             PDF_FILE_WIDE_A3, PDF_FILE_MULTIPLE_A4, PDF_FILE_SINGLE_A4, PDF_FILE_WIDE_A3,
                             PDF_FILE_MULTIPLE_A4 ]

        EXPECTED_FILE_THUMBNAIL_SINGLE_A4 = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_THUMBNAIL_SINGLE_A4_NAME)
        EXPECTED_FILE_THUMBNAIL_WIDE_A3 = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_THUMBNAIL_WIDE_A3_NAME)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0 = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0_NAME)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1 = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1_NAME)
        EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2 = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2_NAME)
        EXPECTED_FILE_INVALID_PDF_THUMBNAIL = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_THUMBNAIL_INVALID_PDF_NAME)
        EXPECTED_FILE_THUMBNAIL_PAGE = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_THUMBNAIL_PAGE_NAME)
        EXPECTED_FILE_PDFTOPPM_THUMBNAIL_PAGE = RESOURCES_DIRECTORY.resolve(EXPECTED_FILE_PDFTOPPM_THUMBNAIL_PAGE_NAME)

        DEFAULT_PARAMETERS = new ThumbnailParameters(thumbnailHeight: 250, useAffineTransformation: false,
                textJustification: ThumbnailParameters.TextJustification.RIGHT)
    }

    @Test
    void verifySingleA4PageGeneratesExpectedThumbnail() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_SINGLE_A4, [ EXPECTED_FILE_THUMBNAIL_SINGLE_A4 ], parameters)
    }

    @Test
    void verifyWideA3PageGeneratesExpectedThumbnail() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_WIDE_A3, [ EXPECTED_FILE_THUMBNAIL_WIDE_A3 ], parameters)
    }

    @Test
    void verifyMultipleA4GeneratesExpectedThumbnails() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_MULTIPLE_A4,
                [ EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_0, EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_1,
                  EXPECTED_FILE_THUMBNAIL_MULTIPLE_A4_2 ], parameters)
    }

    @Test
    void verifyInvalidPdfGeneratesExpectedThumbnail() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        verifyGeneratorCreatesExpectedThumbnails(PDF_FILE_INVALID_PDF, [ EXPECTED_FILE_INVALID_PDF_THUMBNAIL ],
                parameters)
    }

    @Test
    void verifyTestPage1GeneratesAsExpected() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()
        parameters.maximumPageWidth = 1200
        parameters.pageTitleText = "ThumbnailGeneratorTest with ${PDF_FILES_LIST_1.size()} pdf files"

        verifyGeneratorCreatesExpectedThumbnailPage(PDF_FILES_LIST_1, EXPECTED_FILE_THUMBNAIL_PAGE, parameters)
    }

    // We ignore this test as there's no guarantee that pdftoppm is installed on the test system.
    // Ensure that this file is checked in with the @Ignore NOT commented out.
    @Test
    @Ignore
    void verifyTestPage1GeneratesWithPdftoppmAsExpected() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        parameters.generateWithPdftoppm = true
        parameters.maximumPageWidth = 1200
        parameters.pageTitleText = "ThumbnailGeneratorTest (pdftoppm) with ${PDF_FILES_LIST_1.size()} pdf files"

        verifyGeneratorCreatesExpectedThumbnailPage(PDF_FILES_LIST_1, EXPECTED_FILE_PDFTOPPM_THUMBNAIL_PAGE, parameters)
    }

    // This method generates the PDF test page. If the algorithm or layout/format changes, then regenerate the test
    // page and check it in as required.
    // The generated jpeg will appear in the System temp directory.
    @Test
    @Ignore
    void generateTestPageJpeg() {
        File pdfPageFile = File.createTempFile("GENERATED_PDF_PAGE_TEST_", ".jpeg")
        pdfPageFile.deleteOnExit()

        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()
        parameters.maximumPageWidth = 1200
        parameters.pageTitleFontJustification = ThumbnailParameters.TextJustification.RIGHT
        parameters.pageTitleText = "ThumbnailGeneratorTest with ${PDF_FILES_LIST_1.size()} pdf files"

        ThumbnailGenerator.writeThumbnailPage(PDF_FILES_LIST_1, parameters, pdfPageFile.toPath())
    }

    // This method generates the PDF test page using pdftoppm. If the algorithm or layout/format changes, then
    // regenerate the test page and check it in as required.
    // The generated jpeg will appear in the System temp directory.
    @Test
    @Ignore
    void generateTestPageJpegUsingCommandLinePdfToThumbnailFileGenerator() {
        Path pdfPageFile = File.createTempFile("GENERATED_PDF_PAGE_TEST_pdftoppm_", ".jpeg").toPath()
        //pdfPageFile.toFile().deleteOnExit()

        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        parameters.generateWithPdftoppm = true
        parameters.maximumPageWidth = 1200
        parameters.pageTitleFontJustification = ThumbnailParameters.TextJustification.RIGHT
        parameters.pageTitleText = "ThumbnailGeneratorTest (pdftoppm) with ${PDF_FILES_LIST_1.size()} pdf files"

        ThumbnailGenerator.writeThumbnailPage(PDF_FILES_LIST_1, parameters, pdfPageFile)
    }

    // This method generates the given PDF thumbnails. If the algorithm or layout/format changes, then regenerate the
    // test thumbnails and check them in as required.
    // The generated thumbnails will appear in the System temp directory.
    @Test
    @Ignore
    void generateTestPdfThumbnails() {
        ThumbnailParameters parameters = DEFAULT_PARAMETERS.clone()

        generatePdfThumbnail(PDF_FILE_SINGLE_A4, "THUMBNAIL_SINGLE_A4_", parameters)
        generatePdfThumbnail(PDF_FILE_WIDE_A3, "THUMBNAIL_WIDE_A3_", parameters)
        generatePdfThumbnails(PDF_FILE_MULTIPLE_A4, parameters)
        generatePdfThumbnail(PDF_FILE_INVALID_PDF, "THUMBNAIL_INVALID_PDF_", parameters)
    }
}
