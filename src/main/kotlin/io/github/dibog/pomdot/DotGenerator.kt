package io.github.dibog.pomdot

object DotGenerator {

    fun toDot(dependency: Dependency) = buildString {
        append("digraph pom {$nl")

        dependency.forEach { append("  ${it.toDotNode()}$nl") }

        dependency.forEach { parent ->
            parent.dependencies.forEach { dep ->
                append("""  "${parent.coordinate.toCanonicalForm()}" -> "${dep.coordinate.toCanonicalForm()}"$nl""")
            }
        }
        append("}$nl")
    }

}