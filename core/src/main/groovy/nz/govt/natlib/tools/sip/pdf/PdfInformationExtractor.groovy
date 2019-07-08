package nz.govt.natlib.tools.sip.pdf

import groovy.util.logging.Log4j2
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.apache.pdfbox.text.PDFTextStripper

import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j2
class PdfInformationExtractor {

    static Map<String, String> extractMetadata(Path pdfFile) {
        Map<String, String> metadataMap = [ : ]
        PDDocument pdDocument
        try {
            pdDocument = PDDocument.load(pdfFile.toFile())
            PDDocumentInformation info = pdDocument.getDocumentInformation()

            metadataMap.put("Page Count", "${pdDocument.getNumberOfPages()}")

            metadataMap.put("Title", "${info.getTitle()}")
            metadataMap.put("Author", "${info.getAuthor()}")
            metadataMap.put("Subject", "${info.getSubject()}")
            metadataMap.put("Keywords", "${info.getKeywords()}")
            metadataMap.put("Creator", "${info.getCreator()}")
            metadataMap.put("Producer", "${info.getProducer()}")
            metadataMap.put("Creation Date", "${info.getCreationDate()}")
            metadataMap.put("Modification Date", "${info.getModificationDate()}")
            metadataMap.put("Trapped", "${info.getTrapped()}")

        } finally {
            if (pdDocument != null) {
                pdDocument.close()
            }
        }
        return metadataMap
    }

    static String extractText(Path pdfFile) {
        String text = ""
        PDDocument pdDocument
        try {
            pdDocument = PDDocument.load(pdfFile.toFile())
            PDFTextStripper stripper = new PDFTextStripper()

            text = stripper.getText(pdDocument)
        } finally {
            if (pdDocument != null) {
                pdDocument.close()
            }
        }

        return text
    }

    static List<String> matchText(Path pdfFile, String regexPattern) {
        log.info("matchText regexPattern=${regexPattern} for pdfFile=${pdfFile.normalize()}")
        List<String> matchingLines = [ ]
        PDDocument pdDocument
        try {
            pdDocument = PDDocument.load(pdfFile.toFile())
            PDFTextStripper stripper = new PDFTextStripper()

            String text = stripper.getText(pdDocument)

            Pattern pattern = Pattern.compile(regexPattern)
            text.eachLine { String line ->
                Matcher matcher = pattern.matcher(line)
                if (matcher.find()) {
                    log.info("Found match for regexPattern=${regexPattern} in line=${line}")
                    matchingLines.add(line)
                }
            }
        } finally {
            if (pdDocument != null) {
                pdDocument.close()
            }
        }

        return matchingLines
    }

}
