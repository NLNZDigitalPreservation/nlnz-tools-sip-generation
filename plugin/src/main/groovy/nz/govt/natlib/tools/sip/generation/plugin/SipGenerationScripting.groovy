package nz.govt.natlib.tools.sip.generation.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Modernisation (m11n) scripting plugin.
 */
class SipGenerationScripting implements Plugin<Project> {
    Project project

    /**
     * Apply this plugin to the given target object.
     * Currently we do nothing, but it does make its dependencies available.
     *
     * @param target The target object
     */
    @Override
    void apply(Project target) {
        this.project = target
    }
}
