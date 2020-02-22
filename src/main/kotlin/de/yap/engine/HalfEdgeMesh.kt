package de.yap.engine

import org.joml.Vector3f
import org.joml.Vector3i

class Vertex {
    var position: Vector3f? = null
    var halfEdge: Int? = null
}

class HalfEdge {
    var originVertex: Int? = null
    var face: Int? = null
    var nextHalfEdge: Int? = null
    var inverseHalfEdge: Int? = null
}

class Face {
    var startHalfEdge: Int? = null
}

class FaceHalfEdgeIterator(val mesh: HalfEdgeMesh, val faceIndex: Int) : Iterator<HalfEdge> {

    override fun hasNext(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun next(): HalfEdge {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class FaceVertexIterator(private val mesh: HalfEdgeMesh, private val faceIndex: Int) : Iterator<Vertex> {

    private val firstVertexIndex: Int?
    private var currentVertexIndex: Int?
    private var hasProvidedFirstVertex: Boolean = false

    init {
        val startHalfEdge = mesh.faces[faceIndex].startHalfEdge
        firstVertexIndex = if (startHalfEdge == null) {
            null
        } else {
            mesh.halfEdges[startHalfEdge].originVertex
        }
        currentVertexIndex = firstVertexIndex
    }

    override fun hasNext(): Boolean {
        if (firstVertexIndex == null || currentVertexIndex == null) {
            return false
        }
        if (!hasProvidedFirstVertex) {
            return true
        }
        return currentVertexIndex != firstVertexIndex
    }

    override fun next(): Vertex {
        val vertex = mesh.vertices[currentVertexIndex!!]
        currentVertexIndex = mesh.halfEdges[mesh.halfEdges[vertex.halfEdge!!].nextHalfEdge!!].originVertex
        hasProvidedFirstVertex = true
        return vertex
    }
}

class HalfEdgeMesh private constructor(
        val vertices: List<Vertex> = emptyList(),
        val halfEdges: List<HalfEdge> = emptyList(),
        val faces: List<Face> = emptyList()
) {

    companion object {
        fun create(points: List<Vector3f>, indices: List<Vector3i>): HalfEdgeMesh {
            val vertices = mutableListOf<Vertex>()
            val halfEdges = mutableListOf<HalfEdge>()
            val faces = mutableListOf<Face>()

            for (point in points) {
                val vertex = Vertex()
                vertex.position = point
                vertices.add(vertex)
            }

            for (faceIndices in indices) {
                val startHalfEdge = halfEdges.size
                val faceIndex = faces.size

                val he1 = HalfEdge()
                he1.originVertex = faceIndices.x
                he1.face = faceIndex
                he1.nextHalfEdge = startHalfEdge + 1
                vertices[faceIndices.x].halfEdge = startHalfEdge

                val he2 = HalfEdge()
                he2.originVertex = faceIndices.y
                he2.face = faceIndex
                he2.nextHalfEdge = startHalfEdge + 2
                vertices[faceIndices.y].halfEdge = startHalfEdge + 1

                val he3 = HalfEdge()
                he3.originVertex = faceIndices.z
                he3.face = faceIndex
                he3.nextHalfEdge = startHalfEdge
                vertices[faceIndices.z].halfEdge = startHalfEdge + 2

                halfEdges.add(he1)
                halfEdges.add(he2)
                halfEdges.add(he3)

                val face = Face()
                face.startHalfEdge = startHalfEdge
                faces.add(face)
            }

            return HalfEdgeMesh(vertices, halfEdges, faces)
        }
    }

    fun faceHalfEdges(faceIndex: Int): FaceHalfEdgeIterator {
        return FaceHalfEdgeIterator(this, faceIndex)
    }

    fun faceVertices(faceIndex: Int): FaceVertexIterator {
        return FaceVertexIterator(this, faceIndex)
    }
}
