package nz.govt.natlib.tools.sip.state

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

enum SipProcessingExceptionReasonType {
    /**
     * The description field can have placeholders for the details, which are provided by the
     * {@link SipProcessingExceptionReason#details}. The values are then substituted into the description.
     * Note that the <em>LAST</em> placeholder will have the exception name substituted in if the
     * {@link #getDescription} method is called with an exception.
     *
     * Note that summary is used as a directory folder name, so it's best to not have odd characters (like spaces).
     */
    FILE_OF_LENGTH_ZERO(false, 'The given file=${a} is of length zero.', "has-zero-length-files"),
    ALL_FILES_CANNOT_BE_PROPERTY_EVALUATED(true, 'All files cannot be properly evaluated.',
            "has-incomprehensible-files"),
    ALL_FILES_CANNOT_BE_PROCESSED(true, 'All files cannot be processed, reason=${a}',
            "has-unprocessable-files"),
    NO_MATCHING_SIP_DEFINITION(true, 'Unable to match an appropriate SIP definition to the files. Detailed reason=${a}.',
            "no-matching-definition"),
    INVALID_PAGE_FILENAME(false, 'The given file=${a} has an invalid filename (the filename does not conform to expected naming convention)',
            "invalid-filenames"),
    INVALID_PDF(false, 'The given file=${a} is an invalid PDF. Validation failure(s)=${b}.',
            "invalid-pdfs"),
    DUPLICATE_FILE(false, 'Duplicate files file1=${a}, file2=${b}.', "duplicate-files"),
    INVALID_PARAMETERS(true, 'Invalid parameters=${a}', "invalid-parameters"),
    MULTIPLE_DEFINITIONS(true, 'Multiple definitions=${a}', "multiple-definitions"),
    MISSING_SEQUENCE_FILES(false, 'One or more skipped sequences precedes these files=${a}',
            "missing-file-sequences"),
    MANUAL_PROCESSING_REQUIRED(false, 'Manual processing specified=${a}', "manual-processing"),
    GENERIC_ONE_PLACE(false, '${a}', "generic-problem"),
    GENERIC_THREE_PLACES(false, '${a} ${b} ${c}', "generic-problem")

    private final boolean fatal
    private final String description
    private final String summary

    SipProcessingExceptionReasonType(boolean fatal, String description, String summary) {
        this.fatal = fatal
        this.description = description
        this.summary = summary
    }

    String getDescription() {
        return this.description
    }

    String isFatal() {
        return this.fatal
    }

    String getSummary() {
        return this.summary
    }

    /**
     * Substitute the list of details into the description. If the <code>exception</exception> is non-null, then
     * the last value is replaced with the exception.
     *
     * @param details
     * @param exception
     * @return
     */
    String getDescription(List<String> details, Exception exception) {
        // We are simply going to count the '$' in the description string and assume that those are the placeholders.
        int numberOfPlaceholders = this.description.count('$')
        char lastLetter = (char)'a' + (char) numberOfPlaceholders - 1
        if (numberOfPlaceholders > 0) {
            SimpleTemplateEngine simpleTemplateEngine = new SimpleTemplateEngine()
            Template template = simpleTemplateEngine.createTemplate(this.description)
            Map<String, String> binding = [ : ]
            // Note we are limited to 26 variables
            for (int placeholderIndex = 0; placeholderIndex < numberOfPlaceholders; placeholderIndex++) {
                char letter = ((char) 'a') + (char) placeholderIndex
                String bindingKey = "${letter}"
                // if an exception exists, it is the last placeholder
                if ((exception != null) && (placeholderIndex == numberOfPlaceholders - 1)) {
                    binding.put(bindingKey, exception.toString())
                } else {
                    if (placeholderIndex < details.size()) {
                        binding.put(bindingKey, details.get(placeholderIndex))
                    } else {
                        // the binding will fail if there isn't a substitution available
                        binding.put(bindingKey, '${' + bindingKey + '}')
                    }
                }
            }
            Writable toStringTemplate = template.make(binding)
            String templateSubstituted = toStringTemplate.toString()
            return templateSubstituted
        } else {
            return this.description
        }
    }
}
