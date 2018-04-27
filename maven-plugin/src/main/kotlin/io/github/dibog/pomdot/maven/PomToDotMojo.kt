package io.github.dibog.pomdot.maven

import org.apache.maven.plugin.AbstractMojo
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
    private var _format : String? = null

    @Parameter(property = "dest")
    private var _outFile : File? = null

    val project by lazy {
        pluginContext["project"] as MavenProject
    }

    val coord by lazy {
        if(_coord!=null) _coord!!
        else project.toCanonicalForm()
    }

    val format by lazy {
        if(_format!=null) _format!!
        else "PLANT_UML"
    }

    val outFile by lazy {
        val fmt = format.toLowerCase()
        val extension = if(fmt=="plant_uml") "plantuml" else fmt
        File("${project.basedir}/target/${coord.artifactId()}.$extension").apply {
            parentFile.mkdirs()
        }
    }

    override fun execute() {

        val tmpFile = Files.createTempFile("pom-to-dot.",".jar")

        javaClass.getResource("/pom-to-dot.jar").openStream().use {
            Files.copy(it, tmpFile, StandardCopyOption.REPLACE_EXISTING)
        }

        try {
            val cmds = listOf(
                    "java",
                    "-jar", tmpFile.toFile().absolutePath,
                    "--coord", coord,
                    "--out-file", outFile.absolutePath,
                    "--output-mode", format
            )

            log.info("Command line: $cmds")

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
                log.info("Successfully generated '${outFile.absolutePath}'")
            }
        }
        finally {
            Files.delete(tmpFile)
        }
    }

    private fun MavenProject.toCanonicalForm() = "$groupId:$artifactId:$version"

    private fun String.artifactId(): String  {
        val first = indexOf(':')
        if(first<0) throw IllegalStateException("Coordinate '$this' has no valid maven coordinate format")
        val second = indexOf(':', first+1)
        if(second<0) throw IllegalStateException("Coordinate '$this' has no valid maven coordinate format")
        return substring(first+1, second)
    }
}
