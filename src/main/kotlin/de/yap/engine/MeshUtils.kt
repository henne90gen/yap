package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3i
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

    private fun addVertex(vertices: MutableList<Vector3f>, line: String, lineNumber: Int) {
        val floats = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .map { s -> s.toFloat() }

        if (floats.size != 3) {
            log.warn("Malformed vertex on line {}", lineNumber)
            return
        }

        vertices.add(Vector3f(floats[0], floats[1], floats[2]))
    }

    private fun addTextureCoord(textCoords: MutableList<Vector2f>, line: String, lineNumber: Int) {
        val floats = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .map { s -> s.toFloat() }

        if (floats.size != 2) {
            log.warn("Malformed texture coordinate on line {}", lineNumber)
            return
        }
        textCoords.add(Vector2f(floats[0], floats[1]))
    }

    private fun addFace(vertIndices: MutableList<Vector3i>, texIndices: MutableList<Vector3i>,
                        normalIndices: MutableList<Vector3i>, line: String, lineNumber: Int) {
        val ints = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .flatMap { s -> s.split("/") }
                .map { s -> s.toInt() }
                .map { i -> i - 1 }

        log.info("ints: $ints")

        if (ints.size != 3) {
            log.warn("Malformed face on line {}", lineNumber)
            return
        }

        vertIndices.add(Vector3i(ints[0], ints[3], ints[6]))
        texIndices.add(Vector3i(ints[1], ints[4], ints[7]))
        normalIndices.add(Vector3i(ints[2], ints[5], ints[8]))
    }

    override fun load(file: File): Mesh? {
        val lines = file.readLines()
        val vertices = mutableListOf<Vector3f>()
        val vertIndices = mutableListOf<Vector3i>()
        val texIndices = mutableListOf<Vector3i>()
        val normalIndices = mutableListOf<Vector3i>()

        val textCoords = mutableListOf<Vector2f>()
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
            if (line.startsWith("vt ")) {
                addTextureCoord(textCoords, line, lineNumber)
            }
            if (line.startsWith("vn ")) {
                // new vertex normal
                continue
            }
            if (line.startsWith("f ")) {
                addFace(vertIndices, texIndices, normalIndices, line, lineNumber)
                continue
            }
            if (line.startsWith("s ")) {
                // TODO what is this option
                continue
            }
            log.info(line)
        }


        val texture = Texture("src/main/resources/textures/grassblock.png")
        return Mesh(vertices, vertIndices, texture, textCoords, texIndices)
    }
}
