package nz.govt.natlib.tools.sip.generation

import com.google.common.net.MediaType

enum MediaMimeType {
    APPLICATION_PDF("pdf", MediaType.PDF)

    String filenameExtension
    MediaType mediaType

    MediaMimeType(String filenameExtension, MediaType mediaType) {
        this.filenameExtension = filenameExtension
        this.mediaType = mediaType
    }

    static MediaMimeType forExtension(String filenameExtension) {
        return MediaMimeType.values().find { MediaMimeType mediaMimeType ->
            mediaMimeType.filenameExtension.toLowerCase() == filenameExtension.toLowerCase()
        }
    }
}
