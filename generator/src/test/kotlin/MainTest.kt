import io.github.dibog.pomdot.PomToDot
import org.slf4j.bridge.SLF4JBridgeHandler

fun main(args: Array<String>) {
    val cmdArgs = arrayOf(
            "--coord", "io.github.dibog:pom-to-dot-demo:1.2.1-SNAPSHOT", "--out-file", "D:\\Dev\\Sources\\io.github.dibog\\pom-to-dot\\demo\\target",
            "--output-mode", "GIF", "--color", "sche", "blue", "--color", "foo", "black"
    )

    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    PomToDot().main(cmdArgs)
}