package io.github.dibog.pomdot

import org.jboss.shrinkwrap.resolver.api.maven.MavenArtifactInfo

class Dependency(private val artifact: MavenArtifactInfo) : Iterable<Dependency> {
    private val _dependencies = mutableSetOf<Dependency>()
    private var fillcolor: String? = null

    val coordinate = artifact.coordinate!!
    val dependencies : Set<Dependency>
        get() = _dependencies


    fun toDotNode(): String {
        return artifact.coordinate.let {
            if(fillcolor==null)
                """"${it.toCanonicalForm()}" [shape=record, label="{ ${it.groupId} | ${it.artifactId} | ${it.version} }" ]"""
            else
                """"${it.toCanonicalForm()}" [shape=record, fillcolor="$fillcolor", style="filled", label="{ ${it.groupId} | ${it.artifactId} | ${it.version} }" ]"""
        }
    }

    override fun iterator(): Iterator<Dependency> {
        val list = mutableListOf<Dependency>()
        val set = mutableSetOf<Dependency>()

        fun collect(dep: Dependency) {
            if(!set.contains(dep)) {
                list.add(dep)
                set.add(dep)

                dep.dependencies.forEach { collect(it) }
            }
        }

        collect(this)

        return list.iterator()
    }

    override fun hashCode() = coordinate .hashCode()

    override fun equals(other: Any?): Boolean {
        val o = other as? Dependency ?: return false
        return coordinate==o.coordinate
    }

    fun addDependency(dependency: Dependency) = _dependencies.add(dependency)

    fun remove(predicate: (Dependency)->Boolean): Dependency? {
        return filter( { !predicate(it) } )
    }

    fun filter(predicate: (Dependency)->Boolean): Dependency? {
        return if(predicate(this)) {
            Dependency(artifact).also { newDep ->
                for(dep in dependencies) {
                    val filtered = dep.filter(predicate)
                    if(filtered!=null) {
                        newDep.addDependency(filtered)
                    }
                }
           }
        }
        else {
            null
        }
    }

    fun paint(colors: List<Pair<Regex,String>>) {
        forEach { dep ->
            colors.forEach { (regex, color) ->
                if(regex.matches(dep.coordinate.groupId)) {
                    dep.fillcolor = color
                }
            }
        }
    }

    override fun toString() = coordinate.toCanonicalForm()
}
