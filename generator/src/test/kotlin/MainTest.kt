import io.github.dibog.pomdot.OutputMode
import io.github.dibog.pomdot.PomToDot
import org.slf4j.bridge.SLF4JBridgeHandler

fun main(args: Array<String>) {
    val cmdArgs = arrayOf(
            "--coord", "io.github.dibog:pom-to-dot:1.0.0",
            "--output-mode", OutputMode.SVG.name,
//            "--executable", "abc",
            "--out-file", "test.svg",
            "--colors", "org.slf4j", "yellow",
            "--colors", "org.jetbrains.kotlin", "green",
            "--colors", "com.github.ajalt", "red",
            "--exclude-dep", "(org.codehaus.plexus|org.apache.maven|org.jboss.shrinkwrap.resolver)"
    )

    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    PomToDot().main(cmdArgs)
}