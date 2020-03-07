package de.yap.engine.mesh

import de.yap.engine.Font
import de.yap.engine.Texture
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.system.MemoryStack
import java.io.File
import java.nio.IntBuffer

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
                val neighborFile = getNeighborFile(file, line.substring(7))
                currentMaterial?.texture = Texture.fromFile(neighborFile)
                continue
            }
            log.info(line)
        }

        if (currentMaterial != null) {
            materialLib[currentMaterial.name] = currentMaterial
        }
    }
}

class MeshUtils {
    companion object {
        private val log: Logger = LogManager.getLogger(MeshUtils::class.java.name)

        fun quad(
                posMin: Vector2f = Vector2f(-0.5F),
                posMax: Vector2f = Vector2f(0.5F),
                texMin: Vector2f = Vector2f(0.0F),
                texMax: Vector2f = Vector2f(1.0F),
                material: Material? = null
        ): Mesh {
            val vertices = listOf(
                    Vector3f(posMin.x, posMin.y, 0.0F),
                    Vector3f(posMax.x, posMax.y, 0.0F),
                    Vector3f(posMin.x, posMax.y, 0.0F),
                    Vector3f(posMax.x, posMin.y, 0.0F)
            )
            val texCoords = listOf(
                    Vector2f(texMin.x, texMax.y),
                    Vector2f(texMax.x, texMin.y),
                    Vector2f(texMin.x, texMin.y),
                    Vector2f(texMax.x, texMax.y)
            )
            val indices = listOf(
                    Vector3i(0, 1, 2),
                    Vector3i(0, 3, 1)
            )
            return Mesh(vertices = vertices, texCoords = texCoords, indices = indices, material = material)
        }

        fun unitCube(): Mesh {
            val vertices = listOf(
                    // back
                    Vector3f(-0.5F, -0.5F, -0.5F), // 0
                    Vector3f(0.5F, -0.5F, -0.5F),  // 1
                    Vector3f(0.5F, 0.5F, -0.5F),   // 2
                    Vector3f(-0.5F, 0.5F, -0.5F),  // 3

                    // front
                    Vector3f(-0.5F, -0.5F, 0.5F),  // 4
                    Vector3f(0.5F, -0.5F, 0.5F),   // 5
                    Vector3f(0.5F, 0.5F, 0.5F),    // 6
                    Vector3f(-0.5F, 0.5F, 0.5F)    // 7
            )
            val indices = listOf(
                    // front
                    Vector3i(0, 1, 2),
                    Vector3i(0, 2, 3),

                    // back
                    Vector3i(4, 5, 6),
                    Vector3i(4, 6, 7),

                    // right
                    Vector3i(5, 1, 2),
                    Vector3i(5, 2, 6),

                    // left
                    Vector3i(0, 4, 7),
                    Vector3i(0, 7, 3),

                    // top
                    Vector3i(7, 6, 2),
                    Vector3i(7, 2, 3),

                    // bottom
                    Vector3i(4, 5, 1),
                    Vector3i(4, 1, 0)
            )
            return Mesh(vertices = vertices, indices = indices)
        }

        /**
         * https://github.com/LWJGL/lwjgl3/blob/18975883e844d9dc53874836ec45257da13085d9/modules/samples/src/test/java/org/lwjgl/demo/stb/Truetype.java
         *
         * FIXME this method has a memory leak in it. Use with caution
         */
        fun text(font: Font, value: String): Mesh {
            var mesh = Mesh(material = font.material)

            val lineOffset = font.lineOffset()
            MemoryStack.stackPush().use { stack ->
                val codePointBuf = stack.ints(0)
                val xPosBuf = stack.floats(0.0f)
                val yPosBuf = stack.floats(0.0f)
                val quad = STBTTAlignedQuad.mallocStack(stack)

                var i = 0
                val to = value.length
                while (i < to) {
                    i += getCodePoint(value, to, i, codePointBuf)

                    val cp = codePointBuf.get(0)
                    if (cp == '\n'.toInt()) {
                        val lineY = yPosBuf.get(0) + lineOffset
                        yPosBuf.put(0, lineY)
                        xPosBuf.put(0, 0.0f)
                        continue
                    }
                    if (cp < 32 || 128 <= cp) {
                        continue
                    }

                    font.getBakedQuad(cp, xPosBuf, yPosBuf, quad)
                    // if (isKerningEnabled() && i < to) {
                    //     getCP(text, to, i, pCodePoint)
                    //     x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0)) * scale)
                    // }

                    // NOTE: The switch of y0 and y1 is intentional.
                    // quad.y_() is in a coordinate system where y increases the further down you go from 0 to 1
                    val x0 = quad.x0()
                    val y0 = -quad.y1()
                    val x1 = quad.x1()
                    val y1 = -quad.y0()

                    val posMin = Vector2f(x0 / font.bitmapWidth, y0 / font.bitmapHeight)
                    val posMax = Vector2f(x1 / font.bitmapWidth, y1 / font.bitmapHeight)
                    val texMin = Vector2f(quad.s0(), quad.t0())
                    val texMax = Vector2f(quad.s1(), quad.t1())
                    mesh = mesh.withMesh(
                            quad(
                                    posMin = posMin, posMax = posMax,
                                    texMin = texMin, texMax = texMax,
                                    material = null
                            )
                    )
                }
            }

            return mesh
        }

        private fun getCodePoint(text: String, to: Int, i: Int, cpOut: IntBuffer): Int {
            val c1 = text[i]
            if (Character.isHighSurrogate(c1) && i + 1 < to) {
                val c2 = text[i + 1]
                if (Character.isLowSurrogate(c2)) {
                    cpOut.put(0, Character.toCodePoint(c1, c2))
                    return 2
                }
            }
            cpOut.put(0, c1.toInt())
            return 1
        }
    }
}
