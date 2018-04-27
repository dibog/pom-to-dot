package io.github.dibog.pomdot

import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import io.github.dibog.pomdot.OutputMode.valueOf
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates.createCoordinate
import java.io.InputStream
import java.io.OutputStream

internal enum class OutputMode(val fmt: String) {
    DOT("dot"),
    PLANT_UML("plantuml"),
    PNG("png"),
    GIF("gif"),
    JPG("jpg"),
    PS("ps"),
    EPS("eps"),
    SVG("svg"),
    BMP("bmp")
}

internal const val nl = "\n"

internal fun StringBuilder.newline() = append( nl )

internal fun MavenCoordinate.toDotNode(re: Regex?) : String {
    val sb = StringBuilder()

    if(re==null || re.matches(groupId)) {
        sb.append(""""${toCanonicalForm()}" [shape=record, label="{ $groupId | $artifactId | $version }"]""")
    }
    else {
        sb.append(""""${toCanonicalForm()}" [shape=record, fillcolor=grey, style=filled, label="{  $groupId | $artifactId | $version }"]""")
    }

    return sb.toString()
}


internal fun RawOption.mavenCoordinate() : NullableOption<MavenCoordinate, MavenCoordinate> = convert("MAVEN_COORD") {
    createCoordinate(it)
}

internal fun RawOption.outputMode() : NullableOption<OutputMode,OutputMode> = convert("MODE") {
    valueOf(it)
}

internal fun RawOption.regex() : NullableOption<Regex, Regex> = convert("RE") { it.toRegex() }

internal fun InputStream.copyTo(out: OutputStream) {
    val buffer = ByteArray(16384)
    out.use { o->
        use { i ->
            while(true) {
                val len = i.read(buffer)
                if(len<0) return
                o.write(buffer, 0, len)
            }
        }
    }
}