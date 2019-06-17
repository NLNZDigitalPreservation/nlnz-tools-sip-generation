package nz.govt.natlib.tools.sip.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage

import java.awt.Point
import java.awt.geom.Point2D

class PdfDimensionFinder {

    static Point getDimensions(final File pdfFile, int pageNumber = 0) {
        Point dimensions = new Point(-1, -1)
        PDDocument pdDocument
        try {
            pdDocument = PDDocument.load(pdfFile)
            int numberOfPages = pdDocument.getNumberOfPages()
            if (numberOfPages > pageNumber) {
                PDPage page = pdDocument.getPage(pageNumber)
                float height = page.getMediaBox().getHeight()
                float width = page.getMediaBox().getWidth()
                // We accept rounding, as we're not trying to be excessively precise.
                dimensions = new Point((int) width, (int) height)
                int rotation = page.getRotation()
                if (rotation == 90 || rotation == 270) {
                    dimensions = new Point((int) dimensions.y, (int) dimensions.x)
                }
            }
        } finally {
            if (pdDocument != null) {
                pdDocument.close()
            }
        }
        return dimensions
    }

    static Point2D.Double getDimensionalRatio(final File pdfFile1, final File pdfFile2, int pdfFile1Page = 0,
                                              int pdfFile2Page = 0) {
        Point pdfFile1Point = getDimensions(pdfFile1, pdfFile1Page)
        Point pdfFile2Point = getDimensions(pdfFile2, pdfFile2Page)

        return getDimensionalRatio(pdfFile1Point, pdfFile2Point)
    }

    static Point2D.Double getDimensionalRatio(Point firstSize, Point secondSize) {
        double xRatio = -1.0
        double yRatio = -1.0
        if (firstSize.x > 0 && secondSize.x > 0) {
            xRatio = ((double) firstSize.x) / ((double) secondSize.x)
        }

        if (firstSize.y > 0 && secondSize.y > 0) {
            yRatio = ((double) firstSize.y) / ((double) secondSize.y)
        }

        return new Point2D.Double(xRatio, yRatio)
    }

    static boolean isSameSize(Point2D.Double ratio, double minimumVariation) {
        return isSameHeight(ratio, minimumVariation) && isSameWidth(ratio, minimumVariation)
    }

    static boolean isSameHeight(Point2D.Double ratio, double minimumVariation) {
        return minimumVariation > Math.abs(1.0 - ratio.y)
    }

    static boolean isSameWidth(Point2D.Double ratio, double minimumVariation) {
        return minimumVariation > Math.abs(1.0 - ratio.x)
    }

    static boolean isDoubleWidth(Point2D.Double ratio, double minimumVariation) {
        return minimumVariation > Math.abs(2.0 - ratio.x)
    }

    static boolean isHalfWidth(Point2D.Double ratio, double minimumVariation) {
        return minimumVariation > Math.abs(0.5 - ratio.x)
    }

    static boolean isSameSize(final File pdfFile1, final File pdfFile2, int pdfFile1Page = 0, int pdfFile2Page = 0,
                              double minimumVariation) {
        Point2D.Double ratio = getDimensionalRatio(pdfFile1, pdfFile2, pdfFile1Page, pdfFile2Page)
        return isSameSize(ratio, minimumVariation)
    }

    static boolean isSameHeight(final File pdfFile1, final File pdfFile2, int pdfFile1Page = 0, int pdfFile2Page = 0,
                                double minimumVariation = 0.01) {
        Point2D.Double ratio = getDimensionalRatio(pdfFile1, pdfFile2, pdfFile1Page, pdfFile2Page)
        return isSameHeightDoubleWidth(ratio, minimumVariation)
    }

    static boolean isSameWidth(final File pdfFile1, final File pdfFile2, int pdfFile1Page = 0, int pdfFile2Page = 0,
                               double minimumVariation = 0.01) {
        Point2D.Double ratio = getDimensionalRatio(pdfFile1, pdfFile2, pdfFile1Page, pdfFile2Page)
        return isSameWidth(ratio, minimumVariation)
    }

    static boolean isSameHeightDoubleWidth(final File pdfFile1, final File pdfFile2, int pdfFile1Page = 0,
                                           int pdfFile2Page = 0, double minimumVariation = 0.01) {
        Point2D.Double ratio = getDimensionalRatio(pdfFile1, pdfFile2, pdfFile1Page, pdfFile2Page)
        return isSameHeightDoubleWidth(ratio, minimumVariation)
    }

    static boolean isSameHeightDoubleWidth(Point2D.Double ratio, double minimumVariation) {
        return isSameHeight(ratio, minimumVariation) && isDoubleWidth(ratio, minimumVariation)
    }

    static boolean isSameHeightHalfWidth(final File pdfFile1, final File pdfFile2, int pdfFile1Page = 0,
                                         int pdfFile2Page = 0, double minimumVariation = 0.01) {
        Point2D.Double ratio = getDimensionalRatio(pdfFile1, pdfFile2, pdfFile1Page, pdfFile2Page)
        return isSameHeightHalfWidth(ratio, minimumVariation)
    }

    static boolean isSameHeightHalfWidth(Point2D.Double ratio, double minimumVariation) {
        return isSameHeight(ratio, minimumVariation) && isHalfWidth(ratio, minimumVariation)
    }
}
