package nz.govt.natlib.tools.sip.pdf.thumbnail

import groovy.transform.Canonical
import groovy.util.logging.Log4j2
import nz.govt.natlib.tools.sip.pdf.PdfValidator
import nz.govt.natlib.tools.sip.pdf.PdfValidatorFactory
import nz.govt.natlib.tools.sip.pdf.PdfValidatorType
import nz.govt.natlib.tools.sip.pdf.thumbnail.ThumbnailParameters.TextJustification
import nz.govt.natlib.tools.sip.state.SipProcessingException
import nz.govt.natlib.tools.sip.state.SipProcessingExceptionReason
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil

import javax.imageio.ImageIO
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.nio.file.Files

@Log4j2
class ThumbnailGenerator {
    @Canonical
    static class ThumbnailPageParameters {
        List<List<BufferedImage>> thumbnailRows = [ ]
        int maximumPageWidth = 0
        int pageHeight
    }

    static void writeImage(BufferedImage bufferedImage, String path, int dpi) {
        ImageIOUtil.writeImage(bufferedImage, path, dpi)
    }

    static void writeThumbnail(File sourcePdfFile, ThumbnailParameters parameters, File thumbnailFile)
            throws IOException {
        List<BufferedImage> thumbnailImages = generateThumbnailImages([sourcePdfFile ], parameters)
        writeImage(thumbnailImages.first(), thumbnailFile.getCanonicalPath(), parameters.dpi)
    }

    static void writeThumbnailPage(List<File> pdfFiles, ThumbnailParameters parameters, File thumbnailPageFile) {
        BufferedImage thumbnailPageImage = thumbnailPage(pdfFiles, parameters)

        ImageIOUtil.writeImage(thumbnailPageImage, thumbnailPageFile.getCanonicalPath(), parameters.dpi)
    }

    static BufferedImage thumbnailPage(List<File> pdfFiles, ThumbnailParameters parameters) {
        List<BufferedImage> thumbnailImages = generateThumbnailImages(pdfFiles, parameters)

        return thumbnailPageImage(thumbnailImages, parameters)
    }

    static BufferedImage thumbnailPageImage(List<BufferedImage> thumbnailImages, ThumbnailParameters parameters) {
        ThumbnailPageParameters pageParameters = splitIntoRows(thumbnailImages, parameters)
        BufferedImage pageImage = new BufferedImage(pageParameters.maximumPageWidth, pageParameters.pageHeight,
                BufferedImage.TYPE_INT_RGB)

        Graphics2D graphics
        try {
            graphics = pageImage.createGraphics()
            graphics.setColor(parameters.backgroundColor)
            graphics.fillRect(0, 0, pageParameters.maximumPageWidth, pageParameters.pageHeight)

            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0))

            // Write each row
            int yPosition = parameters.hasTitleText() ?
                    parameters.heightBetweenThumbnails + parameters.pageTitleTextHeight :
                    0
            yPosition += parameters.heightBetweenThumbnails
            pageParameters.thumbnailRows.each { List<BufferedImage> thumbnailRow ->
                int rowX = parameters.widthBetweenThumbnails
                thumbnailRow.each { BufferedImage thumbnailImage ->
                    graphics.drawImage(thumbnailImage, rowX, yPosition, null)
                    rowX += thumbnailImage.width + parameters.widthBetweenThumbnails
                }
                yPosition += parameters.thumbnailHeight + parameters.heightBetweenThumbnails
            }

            // Write the title
            writeText(graphics, parameters.pageTitleText, parameters.pageTitleFontName, Font.BOLD,
                    parameters.pageTitleFontSize, parameters.fontColor, parameters.pageTitleFontJustification,
                    0, pageImage.width, parameters.pageTitleTextHeight - 2)
        } finally {
            if (graphics != null) {
                graphics.dispose()
            }
        }

        return pageImage
    }

    static ThumbnailPageParameters splitIntoRows(List<BufferedImage> thumbnailImages, ThumbnailParameters parameters) {
        ThumbnailPageParameters pageParameters = new ThumbnailPageParameters()
        pageParameters.pageHeight = parameters.heightBetweenThumbnails
        if (parameters.hasTitleText()) {
            pageParameters.pageHeight += parameters.heightBetweenThumbnails + parameters.pageTitleTextHeight
        }
        int currentWidth = parameters.widthBetweenThumbnails
        List<BufferedImage> currentRow = [ ]
        pageParameters.thumbnailRows.add(currentRow)
        if (thumbnailImages.size() > 0) {
            pageParameters.pageHeight += parameters.thumbnailHeight + parameters.heightBetweenThumbnails
        }
        thumbnailImages.each { BufferedImage thumbnailImage ->
            int currentThumbnailWidth = thumbnailImage.width
            int extraWidth = currentWidth + currentThumbnailWidth + parameters.widthBetweenThumbnails
            if (extraWidth > parameters.maximumPageWidth) {
                currentRow = [ ]
                pageParameters.thumbnailRows.add(currentRow)
                currentRow.add(thumbnailImage)
                currentWidth = (parameters.widthBetweenThumbnails * 2) + thumbnailImage.width
                pageParameters.pageHeight += parameters.thumbnailHeight + parameters.heightBetweenThumbnails
            } else {
                currentRow.add(thumbnailImage)
                currentWidth = extraWidth
                if (currentWidth > pageParameters.maximumPageWidth) {
                    pageParameters.maximumPageWidth = currentWidth
                }
            }
        }
        //pageParameters.pageHeight += parameters.heightBetweenThumbnails

        return pageParameters
    }

    static List<BufferedImage> generateThumbnailImages(List<File> pdfFiles, ThumbnailParameters parameters) {
        List<BufferedImage> bufferedImages = [ ]
        pdfFiles.each { File pdfFile ->
            bufferedImages.addAll(generateImagesFromPdf(pdfFile, parameters))
        }
        return bufferedImages
    }

    static List<BufferedImage> generateImagesFromPdf(File pdfFile, ThumbnailParameters parameters) throws IOException {
        List<BufferedImage> images
        if (parameters.generateWithPdftoppm) {
            images = generateImagesFromPdfWithPdftoppm(pdfFile, parameters)
        } else {
            images = generateImagesFromPdfWithRenderer(pdfFile, parameters)
        }

        return images
    }

    static List<BufferedImage> generateImagesFromPdfWithRenderer(File pdfFile, ThumbnailParameters parameters)
            throws IOException {
        PdfValidator pdfValidator = PdfValidatorFactory.getValidator(PdfValidatorType.JHOVE_VALIDATOR)
        SipProcessingException sipProcessingException = pdfValidator.validatePdf(pdfFile.toPath())
        List<BufferedImage> images = [ ]
        if (sipProcessingException == null) {
            PDDocument document
            try {
                document = PDDocument.load(pdfFile)
                PDFRenderer pdfRenderer = new PDFRenderer(document)
                int numberOfPages = document.getNumberOfPages()
                for (int page = 0; page < numberOfPages; ++page) {
                    BufferedImage pdfImage = pdfRenderer.renderImageWithDPI(page, parameters.dpi, ImageType.RGB)
                    log.debug("BufferedImage for pdf=${pdfFile.getCanonicalPath()}, page=${page}, width=${pdfImage.width}, height=${pdfImage.height}")
                    String pageNumber = numberOfPages > 1 ? " - ${page}" : ""
                    String caption = "${pdfFile.getName()}${pageNumber}"
                    BufferedImage scaledWithTextImage = scaleAndWriteCaption(pdfImage, parameters, caption)
                    images.add(scaledWithTextImage)
                }
            } finally {
                if (document != null) {
                    document.close()
                }
            }
        } else {
            SipProcessingExceptionReason reason = sipProcessingException.reasons.isEmpty() ? null :
                    sipProcessingException.reasons.first()
            String reasonDescription = reason == null ? "no reason given" : reason.toString()
            images.add(errorImage(reasonDescription, pdfFile.getName(), parameters))
        }
        return images
    }

    static List<BufferedImage> generateImagesFromPdfWithPdftoppm(File pdfFile, ThumbnailParameters parameters) {

        List<BufferedImage> images = [ ]
        List<File> thumbnailFiles = [ ]
        try {
            File tempDirectory = Files.createTempDirectory(FileUtils.tempDirectory.toPath(), "ThumbnailGenerator_").toFile()
            tempDirectory.deleteOnExit()
            boolean wouldHaveCaption = true
            boolean throwExceptionOnFailure = true
            thumbnailFiles = CommandLinePdfToThumbnailFileGenerator.generateThumbnails(pdfFile, tempDirectory,
                    FilenameUtils.removeExtension(pdfFile.name), ".png", parameters, wouldHaveCaption,
                    throwExceptionOnFailure)
        } catch (SipProcessingException sipProcessingException) {
            SipProcessingExceptionReason reason = sipProcessingException.reasons.isEmpty() ? null :
                    sipProcessingException.reasons.first()
            String reasonDescription = reason == null ? "no reason given" : reason.toString()
            images.add(errorImage(reasonDescription, pdfFile.getName(), parameters))
        }
        int numberOfPages = thumbnailFiles.size()
        thumbnailFiles.eachWithIndex { File thumbnailFile, int page ->
            BufferedImage pdfImage = ImageIO.read(thumbnailFile)
            String pageNumber = numberOfPages > 1 ? " - ${page}" : ""
            String caption = "${pdfFile.getName()}${pageNumber}"
            BufferedImage scaledWithTextImage = scaleAndWriteCaption(pdfImage, parameters, caption)
            images.add(scaledWithTextImage)
            thumbnailFile.delete()
        }

        return images
    }

    static BufferedImage errorImage(String reason, String caption, ThumbnailParameters parameters) {
        boolean hasCaption = caption != null && caption.length() > 0
        int scaledHeight = hasCaption ?
                parameters.thumbnailHeight - parameters.textHeight :
                parameters.thumbnailHeight
        // Make the image a wide A3 scale so the error is readable (A-dimensions are 1:sqrt(2))
        int scaledWidth = scaledHeight * Math.sqrt(2.0)
        BufferedImage errorImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)

        Graphics2D graphics
        BufferedImage errorWithTextImage
        try {
            graphics = errorImage.createGraphics()
            graphics.setColor(parameters.errorColor)
            graphics.fillRect(0, 0, scaledWidth, scaledHeight)

            errorWithTextImage = errorImage
            if (reason != null && !reason.isBlank()) {
                int textX = 2
                int textY = scaledHeight / 2
                writeMultilineText(graphics, reason, parameters.fontName, Font.BOLD, parameters.fontSize,
                        parameters.fontColor, textX, scaledWidth, 0, scaledHeight, parameters)
            }
        } finally {
            if (graphics != null) {
                graphics.dispose()
            }
        }

        if (hasCaption) {
            errorWithTextImage = writeCaption(errorImage, parameters, caption)
        }
        return errorWithTextImage
    }

    static BufferedImage scaleAndWriteCaption(BufferedImage originalImage, ThumbnailParameters parameters,
                                           String caption) {
        boolean hasCaption = caption != null && caption.length() > 0
        int scaledHeight = parameters.adjustedThumbnailHeight(hasCaption)
        int scaledWidth = (originalImage.width * scaledHeight) / originalImage.height
        BufferedImage scaledImage = scale(originalImage, scaledWidth, scaledHeight,
                parameters.useAffineTransformation)
        BufferedImage scaledWithTextImage = scaledImage
        if (hasCaption) {
            scaledWithTextImage = writeCaption(scaledImage, parameters, caption)
        }
        return scaledWithTextImage
    }

    static BufferedImage scale(BufferedImage originalImage, int width, int height, boolean useAffineTransform) {
        // Create new (blank) image of required (scaled) size
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        // The difference between Graphics2D and Affine may only be in image quality.
        // Do some performance tests to determine which is faster.
        if (useAffineTransform) {
            double xScale = (double) width / (double) originalImage.getWidth()
            double yScale = (double) height / (double) originalImage.getHeight()
            log.debug("xScale=${xScale}, yScale=${yScale}")
            AffineTransform affineTransform = AffineTransform.getScaleInstance(xScale, yScale)
            AffineTransformOp scaleOperation = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR)
            scaleOperation.filter(originalImage, scaledImage)
        } else {
            Graphics2D graphics
            try {
                // Paint scaled version of image to new image
                graphics = scaledImage.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                graphics.drawImage(originalImage, 0, 0, width, height, null)
            } finally {
                // clean up
                if (graphics != null) {
                    graphics.dispose()
                }
            }
        }
        return scaledImage
    }

    static BufferedImage writeCaption(BufferedImage sourceImage, ThumbnailParameters parameters, String caption) {
        BufferedImage image = new BufferedImage(sourceImage.width, parameters.thumbnailHeight,
            BufferedImage.TYPE_INT_RGB)
        Graphics2D graphics
        try {
            graphics = image.createGraphics()

            graphics.setColor(parameters.captionBackgroundColor)
            graphics.fillRect(0, 0, sourceImage.width, parameters.thumbnailHeight)

            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0))
            graphics.drawImage(sourceImage, 0, 0, null)

            writeText(graphics, caption, parameters.fontName, Font.BOLD, parameters.fontSize, parameters.fontColor,
                    parameters.textJustification, 0, sourceImage.width - 2, parameters.thumbnailHeight - 2)

        } finally {
            if (graphics != null) {
                graphics.dispose()
            }
        }

        return image
    }

    static void writeMultilineText(Graphics2D graphics, String text, String fontName, int fontStyle, int fontSize,
                          Color fontColor, int xLeftLocation, int xRightLocation, int yStartLocation, int yEndLocation,
                                   ThumbnailParameters parameters) {
        int lineHeight = fontSize + parameters.distanceBetweenFontLines
        int yLocation = yStartLocation + lineHeight
        int width = xRightLocation - xLeftLocation - 7
        Font font = new Font(fontName, fontStyle, fontSize)
        FontMetrics fontMetrics = graphics.getFontMetrics(font)
        boolean hasMoreLines = true
        int startingTextIndex = 0
        int endingTextIndex
        while (hasMoreLines) {
            endingTextIndex = nextLineEndingPosition(text, startingTextIndex, width, fontMetrics)
            if (endingTextIndex > startingTextIndex) {
                String textLine = text.substring(startingTextIndex, endingTextIndex)
                writeText(graphics, textLine, fontName, fontStyle, fontSize, fontColor, TextJustification.LEFT,
                        xLeftLocation, xRightLocation, yLocation)
                startingTextIndex = endingTextIndex
            } else {
                hasMoreLines = false
            }
            yLocation += lineHeight
            if (yLocation > yEndLocation) {
                hasMoreLines = false
            }
        }
    }

    static int nextLineEndingPosition(String text, int startingIndex, int maximumWidth, FontMetrics fontMetrics) {
        int currentIndex = startingIndex
        boolean canFitMore = true
        while (canFitMore) {
            int textWidth = fontMetrics.stringWidth(text.substring(startingIndex, currentIndex))
            if (textWidth > maximumWidth) {
                canFitMore = false
            } else {
                if (currentIndex >= text.length()) {
                    canFitMore = false
                } else {
                    currentIndex++
                }
            }
        }
        return currentIndex
    }

    static void writeText(Graphics2D graphics, String text, String fontName, int fontStyle, int fontSize,
                          Color fontColor, TextJustification justification, int xLeftLocation, int xRightLocation,
                          int yLocation) {
        if (text != null && text.length() > 0) {
            graphics.setColor(fontColor)
            Font font = new Font(fontName, fontStyle, fontSize)
            graphics.setFont(font)
            switch (justification) {
                case TextJustification.LEFT:
                    graphics.drawString(text, xLeftLocation, yLocation)
                    break
                case TextJustification.RIGHT:
                    int textWidth = graphics.getFontMetrics().stringWidth(text)
                    graphics.drawString(text, xRightLocation - textWidth, yLocation)
                    break
                case TextJustification.CENTER:
                    int textWidth = graphics.getFontMetrics().stringWidth(text)
                    int xCenter = xLeftLocation + ((xRightLocation - xLeftLocation) / 2)
                    int xStart = xCenter - (textWidth / 2)
                    graphics.drawString(text, xStart, yLocation)
                    break
                default:
                    log.error("Unrecognized justification=${justification}")
                    break
            }
        }
    }
}
