package io.github.dibog.pomdot.maven

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Mojo( name="generate" )
class PomToDotMojo : AbstractMojo() {

    @Parameter(property = "coord")
    private var _coord : String? = null

    @Parameter(property = "format")
    private var format : String? = null

    @Parameter(property = "outFile")
    private var outFile : String? = null

    @Parameter(property = "includeDep")
    private var includeDep : String? = null

    @Parameter(property = "excludeDep")
    private var excludeDep : String? = null

    @Parameter(property = "executable")
    private var executable : String? = null

    @Parameter(property = "colors")
    private var colors = mutableListOf<String>()

    val project by lazy {
        pluginContext["project"] as MavenProject
    }

    val coord by lazy {
        if(_coord!=null) _coord!! else project.toCanonicalForm()
    }

    private fun outFile(): File {
        if(outFile.isNullOrBlank()) return File(project.basedir, "target").ensureDirectoryExists()
        return File(outFile).let {  if(it.isAbsolute) it else File(project.basedir, outFile) }
    }

    private fun validateParameters() {
        if(!includeDep.isNullOrBlank() && !excludeDep.isNullOrBlank()) {
            throw MojoExecutionException("You can only specify one of 'include-dep' and 'exclude-dep'.")
        }

        if(!includeDep.isNullOrBlank()) includeDep!!.toRegex()
        if(!excludeDep.isNullOrBlank()) excludeDep!!.toRegex()
    }

    private fun String.quote() = """"$this""""

    override fun execute() {
        validateParameters()

        val tmpFile = Files.createTempFile("pom-to-dot.",".jar")

        javaClass.getResource("/pom-to-dot.jar").openStream().use {
            Files.copy(it, tmpFile, StandardCopyOption.REPLACE_EXISTING)
        }

        try {
            val cmds = mutableListOf(
                    "java",
                    "-jar", tmpFile.toFile().absolutePath.quote(),
                    "--coord", coord.quote(),
                    "--out-file", outFile().absolutePath.quote()
            )

            format?.let { cmds.addAll(listOf("--output-mode", it.quote()))}
            executable?.let { cmds.addAll(listOf("--executable", it.quote())) }
            includeDep?.let { cmds.addAll(listOf("--include-dep", it.quote())) }
            excludeDep?.let { cmds.addAll(listOf("--exclude-dep", it.quote())) }
            colors.mapNotNull {
                val parts = it.split('=')
                if(parts.size!=2) {
                    log.error("Expected two strigns separated by one '=', but got '$it'")
                    null
                }
                else {
                    parts[0] to parts[1]
                }
            }.forEach { (regex, color) -> cmds.addAll(listOf("--color", regex.quote(), color.quote())) }

//            log.info("Command line: $cmds")

            val proc = ProcessBuilder().command(cmds).start()

            proc.inputStream.use {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use {
                    it.lines().forEach {
                        log.info("  $it")
                    }
                }
            }

            val status = proc.waitFor()
            if(status!=0) {
                throw MojoFailureException(this, "pom-to-dot returned $status", "pom-to-dot returned $status")
            }
            else {
                log.info("Successfully generated graph into '${outFile().absolutePath}'")
            }
        }
        finally {
            Files.delete(tmpFile)
        }
    }

    private fun MavenProject.toCanonicalForm() = "$groupId:$artifactId:$version"
    private fun File.ensureDirectoryExists(): File {
        mkdirs()
        return this
    }
}
