package nz.govt.natlib.tools.sip.generation.components

import groovy.xml.MarkupBuilder

interface ComponentNode {
    List<String> getNamespaces()

    void writeNode(MarkupBuilder markupBuilder)
}