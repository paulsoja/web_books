package com.spasinnya

import java.io.BufferedReader
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.use

object ClasspathBooksLoader {

    fun loadAllJson(): List<Pair<String, String>> {
        val cl = Thread.currentThread().contextClassLoader
        val dirName = "books" // папка в resources

        val url = cl.getResource(dirName)
            ?: return emptyList()

        // file: для разработки; jar: для упакованного JAR
        return when (url.protocol) {
            "file" -> {
                val dirPath = Paths.get(url.toURI())
                Files.list(dirPath)
                    .use { stream ->
                        stream.filter { Files.isRegularFile(it) && it.toString().endsWith(".json", ignoreCase = true) }
                            .map { path -> path.fileName.toString() to Files.readString(path) }
                            .toList()
                    }
            }
            "jar" -> {
                // URL вида: jar:file:/.../app.jar!/books
                val uri = url.toURI()
                // Открываем FS для JAR, если ещё не открыт
                val fs = try {
                    FileSystems.getFileSystem(uri)
                } catch (_: FileSystemNotFoundException) {
                    FileSystems.newFileSystem(uri, emptyMap<String, Any>())
                }

                val dirInJar = fs.getPath("/$dirName")
                require(Files.exists(dirInJar) && dirInJar.isDirectory()) { "Resource dir '/$dirName' not found in JAR" }

                Files.list(dirInJar).use { stream ->
                    stream.filter { Files.isRegularFile(it) && it.toString().endsWith(".json", ignoreCase = true) }
                        .map { p ->
                            val name = p.name
                            val content = Files.newBufferedReader(p).use(BufferedReader::readText)
                            name to content
                        }
                        .toList()
                }
            }
            else -> {
                // fallback: пробуем перечислить ресурсы имён вида books/<file>.json
                // если протокол экзотический — используем ClassLoader.getResources
                val resources = cl.getResources(dirName)
                val all = mutableListOf<Pair<String, String>>()
                while (resources.hasMoreElements()) {
                    val base = resources.nextElement()
                    if (base.protocol == "file") {
                        val basePath = Paths.get(base.toURI())
                        Files.list(basePath).use { s ->
                            s.filter { Files.isRegularFile(it) && it.toString().endsWith(".json", true) }
                                .forEach { path -> all += path.fileName.toString() to Files.readString(path) }
                        }
                    }
                }
                all
            }
        }
    }
}