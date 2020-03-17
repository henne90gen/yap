package de.yap.engine.debug

import de.yap.engine.Window
import de.yap.engine.graphics.Renderer
import de.yap.engine.graphics.Shader
import de.yap.engine.mesh.Mesh
import de.yap.engine.mesh.MeshUtils
import org.joml.Matrix4f

class DebugCPU {

    private var cpuUtilization: Mesh? = null

    fun init(renderer: Renderer) {
        cpuUtilization = MeshUtils.text(renderer.font, "CPU:")
    }

    fun input() {
    }

    fun update(interval: Float) {
    }

    fun render(window: Window, renderer: Renderer, shader: Shader, fontShader: Shader) {
        val cpuUtilizationTextTransform = Matrix4f()
                .translate(0.25F, 0.95F, 0.0F)
                .scale(0.3F)
        renderer.uiText(fontShader, window.aspectRatio(), cpuUtilization!!, cpuUtilizationTextTransform)

        val cpuTransform = Matrix4f()
                .translate(0.4F, 0.95F, 0.0F)
                .scale(0.3F)
        val cpuPercent = 20.0F
        val cpuMesh = MeshUtils.text(renderer.font, "$cpuPercent%")
        renderer.uiText(fontShader, window.aspectRatio(), cpuMesh, cpuTransform)
    }
}
