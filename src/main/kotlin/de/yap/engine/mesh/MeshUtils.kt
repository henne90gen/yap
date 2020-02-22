package de.yap.engine.mesh

import de.yap.engine.Texture
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

    data class FaceIndices(val vertex: Vector3i, val texture: Vector3i, val normal: Vector3i)

    private fun addFace(faceIndices: MutableList<FaceIndices>, line: String, lineNumber: Int) {
        val ints = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .flatMap { s -> s.split("/") }
                .map { s -> s.toInt() }
                .map { i -> i - 1 }

        log.info("ints: $ints")

        if (ints.size != 9) {
            log.warn("Malformed face on line {}", lineNumber)
            return
        }

        faceIndices.add(FaceIndices(
                Vector3i(ints[0], ints[3], ints[6]),
                Vector3i(ints[1], ints[4], ints[7]),
                Vector3i(ints[2], ints[5], ints[8])
        ))
    }


    override fun load(file: File): Mesh? {
        val lines = file.readLines()
        val vertices = mutableListOf<Vector3f>()
        val faceIndices = mutableListOf<FaceIndices>()

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
                continue
            }
            if (line.startsWith("vn ")) {
                // new vertex normal
                continue
            }
            if (line.startsWith("f ")) {
                addFace(faceIndices, line, lineNumber)
                continue
            }
            if (line.startsWith("s ")) {
                // TODO what is this option
                continue
            }
            log.info(line)
        }

        val finalVertexData = mutableListOf<Vector3f>()
        val finalTextureData = mutableListOf<Vector2f>()
        val finalNormalData = mutableListOf<Vector3f>()
        val finalIndices = mutableListOf<Vector3i>()
        for (faceIdx in faceIndices) {
            val elementCount = finalVertexData.size
            finalIndices.add(Vector3i(elementCount, elementCount + 1, elementCount + 2))

            // Todo add normals finalNormalData.add()
            finalVertexData.add(vertices[faceIdx.vertex.x])
            finalTextureData.add(textCoords[faceIdx.texture.x])
            finalVertexData.add(vertices[faceIdx.vertex.y])
            finalTextureData.add(textCoords[faceIdx.texture.y])
            finalVertexData.add(vertices[faceIdx.vertex.z])
            finalTextureData.add(textCoords[faceIdx.texture.z])
        }

        val texture = Texture("src/main/resources/textures/grassblock.png")
        return Mesh(finalVertexData, finalIndices, texture, finalTextureData)
    }
}
