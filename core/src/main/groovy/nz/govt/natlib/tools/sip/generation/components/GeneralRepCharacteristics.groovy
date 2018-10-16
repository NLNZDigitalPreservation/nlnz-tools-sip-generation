package nz.govt.natlib.tools.sip.generation.components

import groovy.transform.Canonical

@Canonical
class GeneralRepCharacteristics  implements ComponentNode {
    String preservationType
    String usageType
    Boolean digitalOriginal
    Integer revisionNumber
}
