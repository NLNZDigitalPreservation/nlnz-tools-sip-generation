package nz.govt.natlib.tools.sip.generation.components

import groovy.transform.Canonical

@Canonical
class GeneralFileCharacteristics  implements ComponentNode {
    Date fileCreationDate
    Date fileModificationDate
    File fileOriginalPath
    String fileOriginalName
    Long fileSizeBytes
    String label

    // TODO maybe a sorting helper method (sort on label? or add an order attribute? Or generate it?)
}
