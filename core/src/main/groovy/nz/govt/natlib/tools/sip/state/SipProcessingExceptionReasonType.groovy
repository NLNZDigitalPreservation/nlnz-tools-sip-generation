package nz.govt.natlib.tools.sip.state

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

enum SipProcessingExceptionReasonType {
    /**
     * The description field can have placeholders for the details, which are provided by the
     * {@link SipProcessingExceptionReason#details}. The values are then substituted into the description.
     * Note that the <em>LAST</em> placeholder will have the exception name substituted in if the
     * {@link #getDescription} method is called with an exception.
     */
    FILE_OF_LENGTH_ZERO(false, 'The given file=${a} is of length zero.'),
    ALL_FILES_CANNOT_BE_PROPERTY_EVALUATED(true, 'All files cannot be properly evaluated.'),
    INVALID_PAGE_FILENAME(false, 'The given file=${a} has an invalid filename (the filename does not conform to expected naming convention)'),
    INVALID_PDF(false, 'The given file=${a} is an invalid PDF. Validation failure(s)=${b}.'),
    GENERIC_THREE_PLACES(false, '${a} ${b} ${c}')

    private final boolean fatal
    private final String description

    SipProcessingExceptionReasonType(boolean fatal, String description) {
        this.fatal = fatal
        this.description = description
    }

    String getDescription() {
        return this.description
    }

    String isFatal() {
        return this.fatal
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
