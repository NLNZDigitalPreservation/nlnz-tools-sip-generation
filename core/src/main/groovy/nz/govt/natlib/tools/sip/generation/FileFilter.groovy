package nz.govt.natlib.tools.sip.generation

import java.nio.file.Path

interface FileFilter {
    boolean matches(Path file)
}
