package io.github.dibog.pomdot

import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate


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

