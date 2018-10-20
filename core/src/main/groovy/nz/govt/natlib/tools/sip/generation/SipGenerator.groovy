package nz.govt.natlib.tools.sip.generation

import com.exlibris.core.sdk.formatting.DublinCore
import com.exlibris.dps.sdk.deposit.IEParser
import com.exlibris.dps.sdk.deposit.IEParserFactory

class SipGenerator {
    private SipParameters sipParameters
    private Map<String, String> parameterOverrides
    private List<FileWrapper> wrappedFiles

    String generateSip(SipParameters sipParameters, Map<String, String> parameterOverrides, List<FileWrapper> wrappedFiles) {
        IEParser ieParser = IEParserFactory.create()

        DublinCore dublinCore = ieParser.getDublinCoreParser()
        dublinCore.addElement("dc:creator", getValue("dc:creator", sipParameters, parameterOverrides))
        dublinCore.addElement("dc:identifier", getValue("dc:identifier", sipParameters, ))

    }

    // TODO Add optional exception if value doesn't exist?
    String getValue(String key, SipParameters sipParameters, Map<String, String> parameterOverrides) {
        //
        String value = parameterOverrides.get(key, sipParameters.getValue(key, null))

        return value
    }
}
