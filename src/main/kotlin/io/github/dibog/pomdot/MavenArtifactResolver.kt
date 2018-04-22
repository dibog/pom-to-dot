package io.github.dibog.pomdot

import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate
import java.io.File
import java.net.URL

internal const val nl = "\n"

class MavenArtifactResolver private constructor(private val repoUrl : URL) {
    companion object {
        fun usingLocalRepo(): MavenArtifactResolver {
            val localRepo = File("${System.getProperty("user.home")}/.m2/repository").toURI().toURL()
            return MavenArtifactResolver(localRepo)
        }
    }

    fun fetchArtifacts(coordinate: MavenCoordinate, transitivity: Boolean): List<MavenResolvedArtifact> {
        val fragment1 = Maven.configureResolver()
                .withRemoteRepo("id", repoUrl, "default")
                .resolve(coordinate.toCanonicalForm())

        val fragment2 = if(transitivity)
            fragment1.withTransitivity()
        else
            fragment1.withoutTransitivity()

        val result = fragment2.`as`(MavenResolvedArtifact::class.java)?.toList() ?: listOf()
        return result
    }
}

class DotGenerator(private val resolver: MavenArtifactResolver = MavenArtifactResolver.usingLocalRepo()) {
    private fun MavenCoordinate.toDotNode(re: Regex?) : String {
        val sb = StringBuilder()

        if(re==null || re.matches(groupId)) {
            sb.append(""""${toCanonicalForm()}" [shape=record, label="{ $groupId | $artifactId | $version }"]""")
        }
        else {
            sb.append(""""${toCanonicalForm()}" [shape=record, fillcolor=grey, style=filled, label="{  $groupId | $artifactId | $version }"]""")
        }

        return sb.toString()
    }

    private fun toDot(artifacts: List<MavenResolvedArtifact>, internalDep: Regex? = null, internalOnly: Boolean, excludeDep: Regex? = null, plantUml: Boolean): String {
        fun List<MavenResolvedArtifact>.toDot(sb: StringBuilder, internalDep: Regex?) {
            val list = if(internalOnly) {
                filter { internalDep!!.matches(it.coordinate.groupId) }
            }
            else {
                toList()
            }

            val excludedDepList = if(excludeDep==null) {
                list
            }
            else {
                filter { !excludeDep.matches(it.coordinate.groupId) }
            }

            excludedDepList.forEach { sb.append("""  ${it.coordinate.toDotNode(internalDep)}$nl""") }

            excludedDepList.forEach { artifact ->
                val coordinate = artifact.coordinate
                val resolvedArtifact = resolver.fetchArtifacts(coordinate, false).firstOrNull { it.coordinate==coordinate }

                val dependencies = if(internalOnly) {
                    resolvedArtifact?.dependencies?.filter { internalDep!!.matches( it.coordinate.groupId ) }
                }
                else {
                    resolvedArtifact?.dependencies?.toList()
                }
                val excludedExtDep = if(excludeDep==null)
                    dependencies
                else {
                    dependencies?.filter { !excludeDep.matches( it.coordinate.groupId ) }
                }

                excludedExtDep?.forEach { dependency ->
                    sb.append("""  "${coordinate.toCanonicalForm()}" -> "${dependency.coordinate.toCanonicalForm()}"$nl""")
                }
            }
        }

        val sb = StringBuilder()
        if(plantUml) sb.append("@startdot$nl")
        sb.append("digraph pom {$nl")
        artifacts.toDot(sb, internalDep)
        sb.append("}$nl")
        if(plantUml) sb.append("@enddot$nl")

        return sb.toString()
    }

    fun generateDot(coord: MavenCoordinate, internalDep: Regex?, internalOnly: Boolean, excludeDep: Regex?, plantUml: Boolean): String {
        val result = resolver.fetchArtifacts(coord, true)
        return toDot(result, internalDep, internalOnly, excludeDep, plantUml)
    }
}
