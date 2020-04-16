#version 330

in vec2 outTexCoord;

uniform vec4 color;
uniform sampler2D textureSampler;

out vec4 outColor;

void main() {
    outColor = texture(textureSampler, outTexCoord) * color;
}
