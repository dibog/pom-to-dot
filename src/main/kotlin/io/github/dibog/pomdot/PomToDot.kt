package io.github.dibog.pomdot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates.createCoordinate
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.FileOutputStream
import java.io.OutputStreamWriter

enum class OutputMode { DOT, PLANT_UML }

class PomToDot : CliktCommand(name="pom-to-dot") {
    private val coordinates by option("--coord", help="Maven coordinates. Format: groupId:artifactId:version[:packaging][:classifier]").mavenCoordinate().required()
    private val includeDep by option(help="Regular Expression for only showing the selected ones").regex()
    private val excludeDep by option(help="Regular Expression for excluding the selected ones").regex()
    private val outFile by option(help="File into which the output should be written").file()
    private val outputMode by option().outputMode().default(OutputMode.DOT)
    private val colors by option(help="color map").transformValues(2) { it[0].toRegex() to it[1] }.multiple()

    override fun run() {
        if(includeDep!=null && excludeDep!=null) {
            throw UsageError("You can only specify either --include-dep or --exclude-dep but not both")
        }

        val root = MavenArtifactResolver.usingLocalRepo().buildGraph(coordinates)
        val processed = when {
            includeDep!=null -> root.filter { includeDep!!.matches(it.coordinate.groupId) }
            excludeDep!=null -> root.remove { excludeDep!!.matches(it.coordinate.groupId) }
            else -> root
        }

        processed?.paint(colors)

        val dot = if(processed==null) "digraph pom {$nl  Empty [shape=box]$nl}$nl" else DotGenerator.toDot(processed)

        when(outputMode) {
            OutputMode.PLANT_UML -> {
                val output = """
                    |@startdot
                    |$dot
                    |@enddot""".trimMargin()

                if(outFile==null) {
                    System.out.println(output)
                }
                else {
                    OutputStreamWriter(FileOutputStream(outFile), Charsets.UTF_8).use { it.append(output) }
                }
            }

            OutputMode.DOT -> {
                if(outFile==null) {
                    System.out.println(dot)
                }
                else {
                    OutputStreamWriter(FileOutputStream(outFile), Charsets.UTF_8).use { it.append(dot) }
                }
            }
        }
    }
}

fun RawOption.mavenCoordinate() : NullableOption<MavenCoordinate, MavenCoordinate> = convert("MAVEN_COORD") {
    createCoordinate(it)
}

fun RawOption.outputMode() : NullableOption<OutputMode,OutputMode> = convert("MODE") {
    OutputMode.valueOf(it)
}

fun RawOption.regex() : NullableOption<Regex, Regex> = convert("RE") { it.toRegex() }

fun main(args: Array<String>) {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    PomToDot().main(args)
}
