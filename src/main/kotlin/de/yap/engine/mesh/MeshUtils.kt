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
    fun load(file: File): List<Mesh>
}

class ObjLoader : MeshLoader {
    companion object {
        private val log: Logger = LogManager.getLogger(ObjLoader::class.java.name)
    }

    data class FaceIndices(val vertex: Vector3i, val texture: Vector3i, val normal: Vector3i)

    override fun supports(file: File): Boolean {
        return file.path.endsWith(".obj")
    }

    override fun load(file: File): List<Mesh> {
        val lines = file.readLines()
        val meshes = mutableListOf<Mesh>()
        val vertices = mutableListOf<Vector3f>()
        val normals = mutableListOf<Vector3f>()
        val textureCoords = mutableListOf<Vector2f>()
        val faceIndices = mutableListOf<FaceIndices>()
        var material: Material? = null
        val materialLib = mutableMapOf<String, Material>()

        var lineNumber = 0
        for (line in lines) {
            lineNumber++
            if (line.startsWith("#")) {
                // ignore comments
                continue
            }
            if (line.startsWith("mtllib ")) {
                loadMaterials(materialLib, getNeighborFile(file, line.substring(7)))
                continue
            }
            if (line.startsWith("o ")) {
                // new object
                if (vertices.isEmpty()) {
                    continue
                }
                val mesh = finishMesh(vertices, textureCoords, normals, faceIndices, material)
                meshes.add(mesh)
                continue
            }
            if (line.startsWith("usemtl ")) {
                material = materialLib[line.substring(7)]
                continue
            }
            if (line.startsWith("v ")) {
                addVertex(vertices, line, lineNumber)
                continue
            }
            if (line.startsWith("vt ")) {
                addTextureCoord(textureCoords, line, lineNumber)
                continue
            }
            if (line.startsWith("vn ")) {
                addNormal(normals, line, lineNumber)
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

        val mesh = finishMesh(vertices, textureCoords, normals, faceIndices, material)
        meshes.add(mesh)

        return meshes
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

    private fun addNormal(normals: MutableList<Vector3f>, line: String, lineNumber: Int) {
        val floats = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .map { s -> s.toFloat() }

        if (floats.size != 3) {
            log.warn("Malformed normal on line {}", lineNumber)
            return
        }

        normals.add(Vector3f(floats[0], floats[1], floats[2]))
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

    private fun addFace(faceIndices: MutableList<FaceIndices>, line: String, lineNumber: Int) {
        val ints = line.substring(2)
                .split(" ")
                .filter { s -> s.isNotEmpty() }
                .flatMap { s -> s.split("/") }
                .map { s -> s.toInt() }
                .map { i -> i - 1 }

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

    private fun finishMesh(
            vertices: MutableList<Vector3f>,
            textureCoords: MutableList<Vector2f>,
            normals: MutableList<Vector3f>,
            faceIndices: MutableList<FaceIndices>,
            material: Material?
    ): Mesh {
        val finalVertexData = mutableListOf<Vector3f>()
        val finalTextureData = mutableListOf<Vector2f>()
        val finalNormalData = mutableListOf<Vector3f>()
        val finalIndices = mutableListOf<Vector3i>()
        for (faceIdx in faceIndices) {
            val size = finalVertexData.size
            finalIndices.add(Vector3i(size, size + 1, size + 2))

            finalVertexData.add(vertices[faceIdx.vertex.x])
            finalTextureData.add(textureCoords[faceIdx.texture.x])
            finalNormalData.add(normals[faceIdx.normal.x])

            finalVertexData.add(vertices[faceIdx.vertex.y])
            finalTextureData.add(textureCoords[faceIdx.texture.y])
            finalNormalData.add(normals[faceIdx.normal.y])

            finalVertexData.add(vertices[faceIdx.vertex.z])
            finalTextureData.add(textureCoords[faceIdx.texture.z])
            finalNormalData.add(normals[faceIdx.normal.z])
        }

        return Mesh(finalVertexData, finalTextureData, finalIndices, material)
    }

    private fun getNeighborFile(file: File, fileName: String): File {
        return File(file.parentFile, fileName)
    }

    private fun loadMaterials(materialLib: MutableMap<String, Material>, file: File) {
        if (!file.exists()) {
            log.warn("Could not load material library '{}'", file.absolutePath)
            return
        }

        val lines = file.readLines()
        var currentMaterial: Material? = null
        for (line in lines) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue
            }
            if (line.startsWith("newmtl ")) {
                if (currentMaterial != null) {
                    materialLib[currentMaterial.name] = currentMaterial
                }
                currentMaterial = Material(line.substring(7))
                continue
            }
            if (line.startsWith("map_Kd ")) {
                currentMaterial?.texture = Texture(getNeighborFile(file, line.substring(7)))
                continue
            }
            log.info(line)
        }

        if (currentMaterial != null) {
            materialLib[currentMaterial.name] = currentMaterial
        }
    }
}
