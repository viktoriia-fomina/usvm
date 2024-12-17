package org.usvm.dataflow.jvm.equals

import kotlinx.coroutines.runBlocking
import org.jacodb.api.jvm.JcClasspath
import org.jacodb.api.jvm.JcDatabase
import org.jacodb.api.jvm.ext.methods
import org.jacodb.impl.features.Builders
import org.jacodb.impl.features.InMemoryHierarchy
import org.jacodb.impl.features.Usages
import org.jacodb.impl.features.classpaths.UnknownClasses
import org.jacodb.impl.jacodb
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.test.assertNotNull

class EqualsBenchmarkTest {
    private var benchmarkPath: String =
        "C:\\Users\\vikyf\\Documents\\GitHub\\jackrabbit"
    private var jarPaths: List<File> = collectJarPaths(benchmarkPath)

    private val classpath: List<File>
        get() = jarPaths

    private val db: JcDatabase = runBlocking {
        jacodb {
            loadByteCode(classpath)
            useProcessJavaRuntime()
            keepLocalVariableNames()
            installFeatures(Usages, Builders, InMemoryHierarchy)
        }.also {
            it.awaitBackgroundJobs()
        }
    }

    val cp: JcClasspath = runBlocking {
        db.classpath(classpath, listOf(UnknownClasses))
    }

    @Test
    fun runOnBenchmarkTest() {
        // TODO: don't get all classes for all JARs at the same time.
        val classNames = cp.locations
            .filter { benchmarkPath in it.path }
            .mapNotNull { it.classNames }

        var allHasTopNumber = 0
        var allHasNoTopNumber = 0

        classNames.forEach { classNameSet ->
            val classes = classNameSet.mapNotNull { cp.findClassOrNull(it) }

            // assertTrue(classes.isNotEmpty(), "No classes are loaded")
            // TODO: check not JcUnknownClass es.

            val equalsToAnalyze = classes.count { cls ->
                val equalsMethod = cls.methods.find {
                    it.name == "equals" && it.parameters.size == 1 && it.parameters.first().type.typeName == "java.lang.Object"
                }
                assertNotNull(equalsMethod)
                equalsMethod.enclosingClass.name != "java.lang.Object"
            }
            println("Equals to analyze: $equalsToAnalyze")

            var hasTopNumber = 0
            var hasNoTopNumber = 0

            classes.forEach { cls ->
                val equalsMethod = cls.methods.find { it.name == "equals" }
                assertNotNull(equalsMethod)

                if (equalsMethod.enclosingClass.name != "java.lang.Object") {
                    if (EqualsProcessor.hasTop(cp, equalsMethod)) {
                        ++hasTopNumber
                    } else {
                        ++hasNoTopNumber
                    }
                }
            }

            allHasTopNumber += hasTopNumber
            allHasNoTopNumber += hasNoTopNumber
        }

        println("\n\nHas TOP: $allHasTopNumber")
        println("Has no TOP: $allHasNoTopNumber")
    }

    private fun collectJarPaths(dirPath: String): List<File> {
        val path = Path(dirPath)
        if (!path.exists()) {
            error("Path [$dirPath] does not exist")
        }
        if (!path.isDirectory()) {
            error("Path [$dirPath] is not directory")
        }

        val jarPaths = Files.walk(path)
            .filter { Files.isRegularFile(it) && it.toString().endsWith(".jar") }
            .collect(Collectors.toList())
            .map { it.toFile() }

        return jarPaths
    }
}