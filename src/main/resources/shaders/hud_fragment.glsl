#version 330

in vec2 outTexCoord;

uniform vec4 color;
uniform sampler2D textureSampler;

out vec4 outColor;

void main() {
    vec4 textureColor = texture(textureSampler, outTexCoord);
    outColor = vec4(textureColor.a) * color;
}
