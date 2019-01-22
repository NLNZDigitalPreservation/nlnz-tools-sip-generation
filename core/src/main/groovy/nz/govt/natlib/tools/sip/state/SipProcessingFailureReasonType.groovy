package nz.govt.natlib.tools.sip.state

import groovy.text.SimpleTemplateEngine
import groovy.text.Template

enum SipProcessingFailureReasonType {
    /**
     * The description field can have placeholders for the details, which are provided by the
     * {@link SipProcessingFailureReason#details}. The values are then substituted into the description.
     * Note that the <em>LAST</em> placeholder will have the exception name substituted in if the
     * {@link #getDescription} method is called with an exception.
     */
    FILE_OF_LENGTH_ZERO('The given file=${a} is of length zero.'),
    ALL_FILES_CANNOT_BE_PROPERTY_EVALUATED('All files cannot be properly evaluated.'),
    GENERIC_THREE_PLACES('${a} ${b} ${c}')

    private final String description

    SipProcessingFailureReasonType(String description) {
        this.description = description
    }

    String getDescription() {
        return this.description
    }

    /**
     * Substitute the list of details into the descrption. If the <code>exception</exception> is non-null, then
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
