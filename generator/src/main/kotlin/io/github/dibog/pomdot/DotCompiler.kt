package io.github.dibog.pomdot

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

internal class DotCompiler(private val _cmd: String? = null)
{
    val cmd by lazy { if(_cmd==null) { "dot" } else { _cmd } }

    fun compile(graph: InputStream, dest: OutputStream, format: String) {
        val proc = try {
            ProcessBuilder()
                    .command(cmd, "-T$format")
                    .start()
        }
        catch(e: Exception) {
            System.err.println(e.localizedMessage)
            return
        }

        thread {
            graph.copyTo(proc.outputStream)
        }

        proc.inputStream.copyTo(dest)

        val status = proc.waitFor()
        if(status!=0) {
            throw RuntimeException("Executing '$cmd' failed with exit code: $status")
        }
    }

    fun compile(dot: String, dest: OutputStream, format: String) {
        compile(ByteArrayInputStream(dot.toByteArray(Charsets.UTF_8)), dest, format)
    }
}
