package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

val MESH_LOADERS: List<MeshLoader> = listOf(ObjLoader())

interface MeshLoader {
    fun supports(file: File): Boolean
    fun load(file: File): Mesh?
}

class ObjLoader : MeshLoader {
    companion object {
        private val log: Logger = LogManager.getLogger(ObjLoader::class.java.name)
    }

    override fun supports(file: File): Boolean {
        return file.path.endsWith(".obj")
    }

    private fun addVertex(vertices: MutableList<Float>, line: String, lineNumber: Int) {
        val floats = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .map { s -> s.toFloat() }

        if (floats.size != 3) {
            log.warn("Malformed vertex on line {}", lineNumber)
            return
        }

        vertices.add(floats[0])
        vertices.add(floats[1])
        vertices.add(floats[2])
    }

    private fun addFace(indices: MutableList<Int>, line: String, lineNumber: Int) {
        val ints = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .map { s -> s.split("/")[0] }
                .map { s -> s.toInt() }
                .map { i -> i - 1 }

        if (ints.size != 3) {
            log.warn("Malformed face on line {}", lineNumber)
            return
        }

        indices.add(ints[0])
        indices.add(ints[1])
        indices.add(ints[2])
    }

    override fun load(file: File): Mesh? {
        val lines = file.readLines()
        val vertices = mutableListOf<Float>()
        val indices = mutableListOf<Int>()
        var lineNumber = 0
        for (line in lines) {
            lineNumber++
            if (line.startsWith("#")) {
                // ignore comments
                continue
            }
            if (line.startsWith("o ")) {
                // new object
                continue
            }
            if (line.startsWith("v ")) {
                addVertex(vertices, line, lineNumber)
                continue
            }
            if (line.startsWith("vn ")) {
                // new vertex normal
                continue
            }
            if (line.startsWith("f ")) {
                addFace(indices, line, lineNumber)
                continue
            }
            log.info(line)
        }
        return Mesh(vertices, indices)
    }
}
