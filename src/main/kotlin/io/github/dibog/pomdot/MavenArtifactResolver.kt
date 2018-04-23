package io.github.dibog.pomdot

import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.MavenArtifactInfo
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates
import java.io.File
import java.net.URL

class MavenArtifactResolver private constructor(private val repoUrl : URL) {
    companion object {
        fun usingLocalRepo(): MavenArtifactResolver {
            val localRepo = File("${System.getProperty("user.home")}/.m2/repository").toURI().toURL()
            return MavenArtifactResolver(localRepo)
        }
    }

    private fun fetchArtifacts(coordinate: MavenCoordinate, transitivity: Boolean): List<MavenResolvedArtifact> {
        val fragment1 = Maven.configureResolver()
                .withRemoteRepo("id", repoUrl, "default")
                .resolve(coordinate.toCanonicalForm())

        val fragment2 = if(transitivity)
            fragment1.withTransitivity()
        else
            fragment1.withoutTransitivity()

        return fragment2.`as`(MavenResolvedArtifact::class.java)?.toList() ?: listOf()
    }

    private fun resolveArtifact(coordinate: MavenCoordinate): MavenResolvedArtifact {
        return fetchArtifacts(coordinate, false).first { it.coordinate==coordinate }
    }

    fun buildGraph(coordinate: MavenCoordinate): Dependency {
        val visited = mutableMapOf<MavenCoordinate, Dependency>()

        fun buildGraphRec(artifact: MavenArtifactInfo): Dependency {
            if(visited.containsKey(artifact.coordinate)) return visited[artifact.coordinate]!!

            val dep = Dependency(artifact)
            visited[artifact.coordinate] = dep
            artifact.dependencies.map { resolveArtifact( it.coordinate ) }.forEach {
                dep.addDependency(buildGraphRec(it))
            }
            return dep
        }

        val parent = resolveArtifact(coordinate)
        return buildGraphRec(parent)
    }
}

fun main(args: Array<String>) {
    val parent = MavenArtifactResolver
            .usingLocalRepo()
            .buildGraph(
                    MavenCoordinates.createCoordinate("io.github.dibog:pom-to-dot:1.0.0")
            )

//    val regex = "org.jboss.shrinkwrap.resolver".toRegex()
    println("\nFull tree")
    println( DotGenerator.toDot(parent) )
}