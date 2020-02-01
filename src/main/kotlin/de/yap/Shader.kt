package de.yap

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.GL20.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.stream.Collectors

class Shader(vertexShaderPath: String, fragmentShaderPath: String) {

    private val log: Logger = LogManager.getLogger(this.javaClass.name)

    private var programId: Int = 0

    init {
        programId = glCreateProgram();
        val vertexShader = compile(GL_VERTEX_SHADER, vertexShaderPath)
        val fragmentShader = compile(GL_FRAGMENT_SHADER, fragmentShaderPath)
        link(vertexShader, fragmentShader)
    }

    fun bind() {
        glUseProgram(programId)
    }

    fun compile(type: Int, filePath: String): Int {
        if (!File(filePath).exists()) {
            log.warn("Could not find {}", filePath)
            return 0
        }

        val lines = BufferedReader(FileReader(filePath))
                .lines()
                .collect(Collectors.toList())

        val shaderId = glCreateShader(type)
        glShaderSource(shaderId, lines.joinToString())
        glCompileShader(shaderId)

        // TODO check for errors
        //        int success;
        //        int infoLogLength;
        //        GL_Call(glGetShaderiv(shaderId, GL_COMPILE_STATUS, &success));
        //        GL_Call(glGetShaderiv(shaderId, GL_INFO_LOG_LENGTH, &infoLogLength));
        //        if (infoLogLength > 0) {
        //            std::vector<char> vertexShaderErrorMessage(infoLogLength + 1);
        //            GL_Call(glGetShaderInfoLog(shaderId, infoLogLength, nullptr, &vertexShaderErrorMessage[0]));
        //            std::cout << &vertexShaderErrorMessage[0] << std::endl;
        //        }
        return shaderId;
    }

    fun link(vertexShader: Int, fragmentShader: Int) {
        if (vertexShader == 0 || fragmentShader == 0) {
            log.warn("Could not load shader.")
            return
        }

        glAttachShader(programId, vertexShader)
        glAttachShader(programId, fragmentShader)

        glLinkProgram(programId)

        // TODO check for errors
        //    int success;
        //    int infoLogLength;
        //    GL_Call(glGetProgramiv(newProgramId, GL_LINK_STATUS, &success));
        //    GL_Call(glGetProgramiv(newProgramId, GL_INFO_LOG_LENGTH, &infoLogLength));
        //    if (infoLogLength > 0) {
        //        std::vector<char> programErrorMessage(infoLogLength + 1);
        //        GL_Call(glGetProgramInfoLog(newProgramId, infoLogLength, nullptr, &programErrorMessage[0]));
        //        std::cout << &programErrorMessage[0] << std::endl;
        //    }

        glDetachShader(programId, vertexShader)
        glDetachShader(programId, fragmentShader)

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

    }
}
