package nz.govt.natlib.tools.sip.pdf.thumbnail

import groovy.transform.AutoClone
import groovy.transform.Canonical

import java.awt.Color

@Canonical
@AutoClone
class ThumbnailParameters {
    static enum TextJustification {
        LEFT, RIGHT, CENTER
    }

    // General
    int dpi = 300
    Color backgroundColor = Color.LIGHT_GRAY
    Color fontColor = Color.BLACK
    Color errorColor = Color.YELLOW
    int distanceBetweenFontLines = 3

    int thumbnailHeight = 120
    // Note that 10 point font is 13 pixels, so 2 pixel space
    int textHeight = 15
    int fontSize = 10
    String fontName = "Ariel" // "Times" // "Courier"
    TextJustification textJustification = TextJustification.RIGHT
    boolean useAffineTransformation

    // Page
    int maximumPageWidth
    String pageTitleText = ""
    String pageTitleFontName = "Ariel" // "Times" // "Courier"
    TextJustification pageTitleFontJustification = TextJustification.CENTER
    int pageTitleTextHeight = 24
    int pageTitleFontSize = 22
    int widthBetweenThumbnails = 5
    int heightBetweenThumbnails = 5

    boolean hasTitleText() {
        return !(pageTitleText == null || pageTitleText.isEmpty())
    }
}
