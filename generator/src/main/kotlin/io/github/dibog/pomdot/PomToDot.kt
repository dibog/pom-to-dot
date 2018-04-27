package io.github.dibog.pomdot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

class PomToDot : CliktCommand(name="pom-to-dot") {

    private val coordinates by option("--coord", help="Maven coordinates. Format: groupId:artifactId:version[:packaging][:classifier]").mavenCoordinate().required()
    private val includeDep by option(help="Regular Expression for only showing the selected ones").regex()
    private val excludeDep by option(help="Regular Expression for excluding the selected ones").regex()
    private val color by option(metavar="REGEX COLOR", help="Colors matching groupIds with the specified color.").transformValues(2) { it[0].toRegex() to it[1] }.multiple()
    private val executable : String by option(help="path to Graphviz/dot executable").default("dot")
    private val outFile by option(help="File into which the output should be written").file()
    private val outputMode by option(help="available output modes: DOT, PLANT_UML, PNG, GIF, JPG, BMP, PS, EPS or SVG. Defaults to 'DOT'.").default(OutputMode.DOT.name)

    private fun composeOutputFile(artifactId: String?): File? = when {
        outFile==null -> null
        outFile!!.isDirectory -> { outFile!!.mkdirs(); File(outFile!!, "$artifactId.${outputModeAsEnum().fmt}") }
        outFile!!.name==outFile!!.nameWithoutExtension -> { outFile!!.parentFile.mkdirs(); File(outFile!!.parentFile, "${outFile!!.name}.${outputModeAsEnum().fmt}") }
        else -> outFile!!
    }

    private fun File?.ensureFolder(): File? {
        return when {
            this==null -> null
            this.isDirectory -> {
                mkdirs()
                this
            }
            else -> {
                parentFile.mkdirs()
                this
            }
        }
    }

    private fun outputModeAsEnum(): OutputMode {
        try {
            return OutputMode.valueOf(outputMode.toUpperCase())
        }
        catch(e: Exception) {
            throw UsageError("--output-mode must be one of '${OutputMode.values()}', but was '$outputMode")
        }
    }

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

        processed?.paint(color)

        val dot = if(processed==null) "digraph pom {$nl  Empty [shape=box]$nl}$nl" else DotGenerator.toDot(processed)

        val file = composeOutputFile(coordinates.artifactId)

        when(outputModeAsEnum()) {
            OutputMode.PLANT_UML -> {
                val output = """
                    |@startdot
                    |$dot
                    |@enddot""".trimMargin()


                if(file==null) {
                    System.out.println(output)
                }
                else {
                    OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8).use { it.append(output) }
                }
            }

            OutputMode.DOT -> {
                if(file==null) {
                    System.out.println(dot)
                }
                else {
                    OutputStreamWriter(FileOutputStream(outFile), Charsets.UTF_8).use { it.append(dot) }
                }
            }

            OutputMode.PNG,
            OutputMode.GIF,
            OutputMode.JPG,
            OutputMode.BMP,
            OutputMode.EPS,
            OutputMode.PS,
            OutputMode.SVG -> {
                val out = if(file==null) { System.out } else { FileOutputStream(file) }
                DotCompiler(executable).compile(dot, out, outputModeAsEnum().fmt)
            }
        }
    }
}

fun main(args: Array<String>) {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    PomToDot().main(args)
}
