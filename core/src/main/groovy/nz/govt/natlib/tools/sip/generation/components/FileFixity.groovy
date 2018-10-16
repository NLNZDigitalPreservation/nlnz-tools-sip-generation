package nz.govt.natlib.tools.sip.generation.components

import groovy.transform.Canonical

@Canonical
class FileFixity  implements ComponentNode {
    String fixityType
    String fixityValue
}
