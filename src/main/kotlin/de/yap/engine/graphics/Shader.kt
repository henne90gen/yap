package de.yap.engine.graphics

import de.yap.engine.util.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

abstract class Uniform

data class Matrix4fUniform(val value: Matrix4f) : Uniform()
data class Vector3fUniform(val value: Vector3f) : Uniform()
data class Vector4fUniform(val value: Vector4f) : Uniform()
data class IntUniform(val value: Int) : Uniform()


class Shader(private val vertexShaderPath: String, private val fragmentShaderPath: String) {

    companion object {
        private val log: Logger = LogManager.getLogger()
    }

    private var programId: Int = 0

    private val currentUniforms = LinkedHashMap<String, Uniform>()

    fun compile() {
        programId = glCreateProgram()
        val vertexShader = compilePartial(GL_VERTEX_SHADER, vertexShaderPath)
        val fragmentShader = compilePartial(GL_FRAGMENT_SHADER, fragmentShaderPath)
        link(vertexShader, fragmentShader)
    }

    fun bind() {
        glUseProgram(programId)
    }

    fun unbind() {
        glUseProgram(0)
    }

    fun setUniform(name: String, value: Int) {
        bind()

        val loc = glGetUniformLocation(programId, name)
        if (loc < 0) {
            log.warn("Could not find uniform '{}'", name)
            return
        }

        currentUniforms[name] = IntUniform(value)

        glUniform1i(loc, value)
    }

    fun setUniform(name: String, value: Vector4f) {
        bind()

        val loc = glGetUniformLocation(programId, name)
        if (loc < 0) {
            log.warn("Could not find uniform '{}'", name)
            return
        }

        currentUniforms[name] = Vector4fUniform(value)

        glUniform4f(loc, value.x, value.y, value.z, value.w)
    }

    fun setUniform(name: String, value: Vector3f) {
        bind()

        val loc = glGetUniformLocation(programId, name)
        if (loc < 0) {
            log.warn("Could not find uniform '{}", name)
            return
        }

        currentUniforms[name] = Vector3fUniform(value)

        glUniform3f(loc, value.x, value.y, value.z)
    }

    fun setUniform(name: String, value: Matrix4f) {
        bind()

        val loc = glGetUniformLocation(programId, name)
        if (loc < 0) {
            log.warn("Could not find uniform '{}'", name)
            return
        }

        currentUniforms[name] = Matrix4fUniform(value)

        var buffer: FloatBuffer? = null
        try {
            buffer = MemoryUtil.memAllocFloat(16)
            value.get(buffer)

            glUniformMatrix4fv(loc, false, buffer!!)
        } finally {
            MemoryUtil.memFree(buffer)
        }
    }

    private fun compilePartial(type: Int, filePath: String): Int {
        val lines = IOUtils.loadInternalTextFile(filePath)

        val shaderId = glCreateShader(type)
        val shaderSource = lines.joinToString("\n")
        glShaderSource(shaderId, shaderSource)
        glCompileShader(shaderId)

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            val shaderType = if (type == GL_VERTEX_SHADER) "vertex" else "fragment"
            log.error("Error compiling {} shader code: {}", shaderType, glGetShaderInfoLog(shaderId, 1024))
            return 0
        }

        return shaderId
    }

    private fun link(vertexShader: Int, fragmentShader: Int) {
        if (vertexShader == 0 || fragmentShader == 0) {
            log.warn("Could not load shader.")
            return
        }

        glAttachShader(programId, vertexShader)
        glAttachShader(programId, fragmentShader)

        glLinkProgram(programId)

        glValidateProgram(programId)
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            log.error("Warning validating Shader code: {}", glGetProgramInfoLog(programId, 1024))
        }

        glDetachShader(programId, vertexShader)
        glDetachShader(programId, fragmentShader)

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getUniform(name: String): T? {
        return currentUniforms[name] as T
    }
}
