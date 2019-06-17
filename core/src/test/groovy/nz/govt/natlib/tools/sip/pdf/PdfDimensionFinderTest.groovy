package nz.govt.natlib.tools.sip.pdf

import org.junit.Test

import java.awt.Point
import java.awt.geom.Point2D

import static org.hamcrest.core.Is.is
import static org.hamcrest.Matchers.lessThan
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

class PdfDimensionFinderTest {
    static final String TEST_FILE_LOCATION = "src/test/resources/nz/govt/natlib/tools/sip/pdf/"
    static final String TEST_A3_LANDSCAPE_FILENAME = "A3-landscape-dimensioned.pdf"
    static final String TEST_A3_PORTRAIT_ROTATED_90_FILENAME = "A3-portrait-dimensioned-rotated-90.pdf"
    static final String TEST_A4_PORTRAIT_FILENAME = "A4-portrait-dimensioned.pdf"
    File TEST_A3_LANDSCAPE_FILE = new File(TEST_FILE_LOCATION + File.separator + TEST_A3_LANDSCAPE_FILENAME)
    File TEST_A3_PORTRAIT_ROTATED_90_FILE = new File(TEST_FILE_LOCATION + File.separator + TEST_A3_PORTRAIT_ROTATED_90_FILENAME)
    File TEST_A4_PORTRAIT_FILE = new File(TEST_FILE_LOCATION + File.separator + TEST_A4_PORTRAIT_FILENAME)

    static final Point EXPECTED_A3_LANDSCAPE_DIMENSIONS = new Point(1190, 841)
    static final Point EXPECTED_A3_PORTRAIT_ROTATED_90_DIMENSIONS = new Point(1190, 841)
    static final Point EXPECTED_A4_PORTRAIT_DIMENSIONS = new Point(595, 841)

    static final Point2D.Double EXPECTED_A3_TO_A4_RATIOS = new Point2D.Double(2.001, 1.0)
    static final Point2D.Double EXPECTED_A4_TO_A3_RATIOS = new Point2D.Double(0.5, 1.0)
    static final Point2D.Double EXPECTED_A4_TO_A4_RATIOS = new Point2D.Double(1.0, 1.0)
    static final Point2D.Double EXPECTED_A3_TO_A3_RATIOS = new Point2D.Double(1.0, 1.0)

    @Test
    void a3LandscapeFileDimensionedCorrectly() {
        fileDimensionedCorrectly(TEST_A3_LANDSCAPE_FILE, EXPECTED_A3_LANDSCAPE_DIMENSIONS)
    }

    @Test
    void a3PortraitRotated90FileDimensionedCorrectly() {
        fileDimensionedCorrectly(TEST_A3_PORTRAIT_ROTATED_90_FILE, EXPECTED_A3_PORTRAIT_ROTATED_90_DIMENSIONS)
    }

    @Test
    void a4PortraitFileDimensionedCorrectly() {
        fileDimensionedCorrectly(TEST_A4_PORTRAIT_FILE, EXPECTED_A4_PORTRAIT_DIMENSIONS)
    }

    void fileDimensionedCorrectly(File testFile, Point expectedDimensions) {
        Point dimensions = PdfDimensionFinder.getDimensions(testFile, 0)

        assertThat("PDF dimensions=${dimensions}", dimensions, is(expectedDimensions))
    }

    @Test
    void a3ToA4RatiosCorrectlyDetermined() {
        ratiosCorrectlyDetermined(TEST_A3_LANDSCAPE_FILE, TEST_A4_PORTRAIT_FILE, 0, 0,
                EXPECTED_A3_TO_A4_RATIOS, 0.01)
    }

    @Test
    void a4ToA3RatiosCorrectlyDetermined() {
        ratiosCorrectlyDetermined(TEST_A4_PORTRAIT_FILE, TEST_A3_LANDSCAPE_FILE, 0, 0,
                EXPECTED_A4_TO_A3_RATIOS, 0.01)
    }

    @Test
    void a3ToA3RatiosCorrectlyDetermined() {
        ratiosCorrectlyDetermined(TEST_A3_LANDSCAPE_FILE, TEST_A3_LANDSCAPE_FILE, 0, 0,
                EXPECTED_A3_TO_A3_RATIOS, 0.01)
    }

    @Test
    void a4ToA4RatiosCorrectlyDetermined() {
        ratiosCorrectlyDetermined(TEST_A4_PORTRAIT_FILE, TEST_A4_PORTRAIT_FILE, 0, 0,
                EXPECTED_A4_TO_A4_RATIOS, 0.01)
    }

    void ratiosCorrectlyDetermined(File firstPdfFile, File secondPdfFile, int firstPageNumber, int secondPageNumber,
                                   Point2D.Double expectedRatio, double minimumRatioDifference) {
        Point2D.Double ratios = PdfDimensionFinder.getDimensionalRatio(firstPdfFile, secondPdfFile,
                firstPageNumber, secondPageNumber)
        double xRatioDifference = ratios.x - expectedRatio.x
        double yRatioDifference = ratios.y - expectedRatio.y

        assertThat("x Dimensional ratio=${ratios} is < ${minimumRatioDifference} for expected=${expectedRatio}",
                xRatioDifference, is(lessThan(minimumRatioDifference)))
        assertThat("y Dimensional ratio=${ratios} is < ${minimumRatioDifference} for expected=${expectedRatio}",
                yRatioDifference, is(lessThan(minimumRatioDifference)))
    }

    @Test
    void a3LandscapeIsSameHeightDoubleWidthToA4Portrait() {
        assertTrue("A3 landscape is same height but double width of A4 portrait",
        PdfDimensionFinder.isSameHeightDoubleWidth(TEST_A3_LANDSCAPE_FILE, TEST_A4_PORTRAIT_FILE, 0, 0, 0.01))
    }

    @Test
    void a3PortraitRotated90IsSameHeightDoubleWidthToA4Portrait() {
        assertTrue("A3 portrait rotated 90 is same height but double width of A4 portrait",
                PdfDimensionFinder.isSameHeightDoubleWidth(TEST_A3_PORTRAIT_ROTATED_90_FILE, TEST_A4_PORTRAIT_FILE, 0, 0, 0.01))
    }

    @Test
    void a4PortaitIsSameHeightHalfWidthToA3Landscape() {
        assertTrue("A4 portrait is same height but half width of A3 landscape",
                PdfDimensionFinder.isSameHeightHalfWidth(TEST_A4_PORTRAIT_FILE, TEST_A3_LANDSCAPE_FILE, 0, 0, 0.01))
    }

    @Test
    void a3LandscapeIsNotSameHeightHalfWidthToA4Portrait() {
        assertFalse("A3 landscape is not same height but half width of A4 portrait",
                PdfDimensionFinder.isSameHeightHalfWidth(TEST_A3_LANDSCAPE_FILE, TEST_A4_PORTRAIT_FILE, 0, 0, 0.01))
    }

    @Test
    void a4PortraitIsNotSameHeightDoubleWidthToA3Landscape() {
        assertFalse("A4 portrait is not same height but double width of A3 landscape",
                PdfDimensionFinder.isSameHeightDoubleWidth(TEST_A4_PORTRAIT_FILE, TEST_A3_LANDSCAPE_FILE,  0, 0, 0.01))
    }

    @Test
    void a3LandscapeIsSameSizeTOToA3Landscape() {
        assertTrue("A3 Landscape is same size to A3 Landscape",
                PdfDimensionFinder.isSameSize(TEST_A3_LANDSCAPE_FILE, TEST_A3_LANDSCAPE_FILE, 0, 0, 0.01))
    }

    @Test
    void a4PortaitIsSameSizeToA4Portrait() {
        assertTrue("A4 portrait is same size to A4 portrait",
                PdfDimensionFinder.isSameSize(TEST_A4_PORTRAIT_FILE, TEST_A4_PORTRAIT_FILE, 0, 0, 0.01))
    }

    @Test
    void a3LandscapeIsNotSameSizeToA4Portrait() {
        assertFalse("A3 landscape is not same size of A4 portrait",
                PdfDimensionFinder.isSameSize(TEST_A3_LANDSCAPE_FILE, TEST_A4_PORTRAIT_FILE, 0, 0, 0.01))
    }

    @Test
    void a4PortraitIsNotSameSizeToA3Landscape() {
        assertFalse("A4 portrait is not same size of A3 landscape",
                PdfDimensionFinder.isSameSize(TEST_A4_PORTRAIT_FILE, TEST_A3_LANDSCAPE_FILE,  0, 0, 0.01))
    }

}
