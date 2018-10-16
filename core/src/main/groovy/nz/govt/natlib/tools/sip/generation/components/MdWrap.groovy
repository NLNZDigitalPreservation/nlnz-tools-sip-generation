package nz.govt.natlib.tools.sip.generation.components

import groovy.transform.Canonical

@Canonical
class MdWrap implements ComponentNode {
    GeneralFileCharacteristics generalFileCharacteristics
    FileFixity fileFixity
}
