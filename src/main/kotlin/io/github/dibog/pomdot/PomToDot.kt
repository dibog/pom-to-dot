package io.github.dibog.pomdot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MissingParameter
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates.createCoordinate
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class PomToDot : CliktCommand(name="pom-to-dot") {
    private val coord by option(help="Maven coordinates. Format: groupId:artifactId:version[:packaging][:classifier]").mavenCoordinate().required()
    private val internalDep by option(help="Regular Expression for highlighting the internal dependencies").regex()
    private val internalOnly  by option(help="Shows only the internal dependencies").flag(default=false)
    private val excludeDep by option(help="Regular Expression for excluding dependencies").regex()
    private val outFile by option(help="File into which dot output is written").file()
    private val plantUml by option(help="Should format it for plantUml").flag(default=false)

    override fun run() {
        if(internalOnly && internalDep==null) {
            throw MissingParameter("If you specify --internal-only, you must also set --internalDep")
        }

        val dot= DotGenerator().generateDot(
                coord,
                internalDep,
                internalOnly,
                excludeDep,
                plantUml
        )
        if(outFile==null) {
            System.out.println(dot)
        }
        else {
            OutputStreamWriter(FileOutputStream(outFile), Charsets.UTF_8).use { it.append(dot) }
        }
    }
}

fun RawOption.mavenCoordinate() : NullableOption<MavenCoordinate, MavenCoordinate> = convert("MAVEN_COORD") {
    createCoordinate(it)
}

fun RawOption.regex() : NullableOption<Regex, Regex> = convert("RE") { it.toRegex() }


fun main(args: Array<String>) {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    PomToDot().main(args)
}
