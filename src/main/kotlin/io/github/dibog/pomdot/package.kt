package io.github.dibog.pomdot

import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate

internal enum class OutputMode { DOT, PLANT_UML }

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
    org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates.createCoordinate(it)
}

internal fun RawOption.outputMode() : NullableOption<OutputMode,OutputMode> = convert("MODE") {
    io.github.dibog.pomdot.OutputMode.valueOf(it)
}

internal fun RawOption.regex() : NullableOption<Regex, Regex> = convert("RE") { it.toRegex() }