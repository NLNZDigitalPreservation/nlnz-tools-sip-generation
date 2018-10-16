package nz.govt.natlib.tools.sip.generation.components

import groovy.transform.Canonical

@Canonical
class DublinCoreIdentifier  implements ComponentNode {
    Integer termsAvailable
    String title
    String date
    Integer coverage
}
