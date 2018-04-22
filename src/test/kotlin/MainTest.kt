fun main(args: Array<String>) {
    val cmdArgs = arrayOf(
            "--coord", "io.github.dibog:pom-to-dot:1.0.0-SNAPSHOT",
            "--plant-uml",
            "--out-file", "test.plantuml",
            "--exclude-dep", "(org.codehaus.plexus|org.apache.maven|org.jboss.shrinkwrap.resolver)")

    io.github.dibog.pomdot.main(cmdArgs)
}