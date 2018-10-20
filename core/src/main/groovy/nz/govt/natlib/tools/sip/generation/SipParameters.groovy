package nz.govt.natlib.tools.sip.generation

class SipParameters {
    Map<String, String> parameterMap = [ : ]

    String getValue(String key, String defaultValue) {
        return parameterMap.get(key, defaultValue)
    }

    void setValue(String key, String value) {
        parameterMap.put(key, value)
    }
}
