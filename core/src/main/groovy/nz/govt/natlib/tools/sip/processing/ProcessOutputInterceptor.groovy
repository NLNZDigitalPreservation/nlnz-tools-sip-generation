package nz.govt.natlib.tools.sip.processing

import groovy.ui.SystemOutputInterceptor

import java.nio.file.Path

class ProcessOutputInterceptor {

    PrintWriter outputWriter
    Path path
    SystemOutputInterceptor systemOutOutputInterceptor
    SystemOutputInterceptor systemErrOutputInterceptor

    static ProcessOutputInterceptor forTempFile(String filePrefix = "processOutputIntercept", String fileSuffix = ".tmp",
        boolean deleteOnExit = true) {
        File tempFile = File.createTempFile(filePrefix, fileSuffix)
        Path tempPath = tempFile.toPath()
        if (deleteOnExit) {
            tempFile.deleteOnExit()
        }
        return new ProcessOutputInterceptor(tempPath)
    }

    ProcessOutputInterceptor(Path path) {
        this.path = path
        this.outputWriter = new PrintWriter(path.toFile())
        this.systemOutOutputInterceptor = new SystemOutputInterceptor( { int consoleNumber, String output ->
            this.outputWriter.append(output)
            true
        }, true)
        this.systemErrOutputInterceptor = new SystemOutputInterceptor({ int consoleNumber, String output ->
            this.outputWriter.append(output)
            true
        }, false)
    }

    void start() {
        this.systemOutOutputInterceptor.start()
        this.systemErrOutputInterceptor.start()
    }

    void stop() {
        this.systemOutOutputInterceptor.stop()
        this.systemErrOutputInterceptor.stop()
    }

    void close() {
        this.outputWriter.flush()
        this.outputWriter.close()
    }

    void stopAndClose() {
        this.stop()
        this.close()
    }
}
