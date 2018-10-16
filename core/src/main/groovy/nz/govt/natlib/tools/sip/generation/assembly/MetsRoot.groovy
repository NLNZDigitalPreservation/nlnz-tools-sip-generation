package nz.govt.natlib.tools.sip.generation.assembly

import groovy.transform.Canonical
import nz.govt.natlib.tools.sip.generation.components.AccessRightsPolicy
import nz.govt.natlib.tools.sip.generation.components.AlmaIdentifier
import nz.govt.natlib.tools.sip.generation.components.DublinCoreIdentifier
import nz.govt.natlib.tools.sip.generation.components.GeneralRepCharacteristics
import nz.govt.natlib.tools.sip.generation.components.MdWrap

@Canonical
class MetsRoot {
    DublinCoreIdentifier dublinCoreIdentifier
    AlmaIdentifier almaIdentifier
    AccessRightsPolicy accessRightsPolicy
    GeneralRepCharacteristics generalRepCharacteristics
    List<MdWrap> wraps
}
