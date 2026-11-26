package com.campushangout.server.architecture

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertTrue

/**
 * Keeps the areas independent (see docs/team/TEAM_GUIDE.md section 5):
 * - social never imports account/profile, and the other way round
 * - common and contract never import any area
 * Cross-area needs go through an interface in `contract`.
 */
class PackageBoundaryTest {

    /** Gradle runs tests with the `server` folder as the working directory. */
    private val sourceRoot: Path = Paths.get("src/main/kotlin/com/campushangout/server")

    private fun forbiddenImports(fromPackages: List<String>, forbiddenPackages: List<String>): List<String> =
        fromPackages.flatMap { pkg ->
            val dir = sourceRoot.resolve(pkg)
            if (!Files.isDirectory(dir)) return@flatMap emptyList<String>()

            val files = Files.walk(dir).use { paths ->
                paths.filter { it.toString().endsWith(".kt") }.toList()
            }
            files.flatMap { file ->
                Files.readAllLines(file).withIndex()
                    .filter { (_, line) ->
                        forbiddenPackages.any { forbidden ->
                            line.trim().startsWith("import com.campushangout.server.$forbidden.")
                        }
                    }
                    .map { (index, line) -> "${sourceRoot.relativize(file)}:${index + 1}  ${line.trim()}" }
            }
        }

    private fun assertNone(found: List<String>, rule: String) {
        assertTrue(
            found.isEmpty(),
            "$rule. Use an interface in the contract package instead:\n" + found.joinToString("\n"),
        )
    }

    @Test
    fun `source folder is found`() {
        assertTrue(Files.isDirectory(sourceRoot), "Run tests from the server folder: $sourceRoot not found")
    }

    @Test
    fun `social does not import account or profile`() {
        assertNone(forbiddenImports(listOf("social"), listOf("account", "profile")), "social must not import account/profile")
    }

    @Test
    fun `account and profile do not import social`() {
        assertNone(forbiddenImports(listOf("account", "profile"), listOf("social")), "account/profile must not import social")
    }

    @Test
    fun `common and contract do not import any area`() {
        assertNone(
            forbiddenImports(listOf("common", "contract"), listOf("account", "profile", "social")),
            "common/contract must not import area packages",
        )
    }
}
