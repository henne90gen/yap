package de.yap.engine

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.GL20.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.stream.Collectors

class Shader(private val vertexShaderPath: String, private val fragmentShaderPath: String) {

    private val log: Logger = LogManager.getLogger(this.javaClass.name)

    private var programId: Int = 0

    fun compile() {
        programId = glCreateProgram();
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

    private fun compilePartial(type: Int, filePath: String): Int {
        if (!File(filePath).exists()) {
            log.warn("Could not find {}", filePath)
            return 0
        }

        val lines = BufferedReader(FileReader(filePath))
                .lines()
                .collect(Collectors.toList())

        val shaderId = glCreateShader(type)
        val shaderSource = lines.joinToString("\n")
        glShaderSource(shaderId, shaderSource)
        glCompileShader(shaderId)

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            val shaderType = if (type == GL_VERTEX_SHADER) "vertex" else "fragment"
            log.error("Error compiling {} shader code: {}", shaderType, glGetShaderInfoLog(shaderId, 1024))
            return 0
        }

        return shaderId;
    }

    private fun link(vertexShader: Int, fragmentShader: Int) {
        if (vertexShader == 0 || fragmentShader == 0) {
            log.warn("Could not load shader.")
            return
        }

        glAttachShader(programId, vertexShader)
        glAttachShader(programId, fragmentShader)

        glLinkProgram(programId)

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            log.error("Warning validating Shader code: {}", glGetProgramInfoLog(programId, 1024));
        }

        glDetachShader(programId, vertexShader)
        glDetachShader(programId, fragmentShader)

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
    }
}
