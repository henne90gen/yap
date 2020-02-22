#version 330

uniform vec4 color;

in  vec2 outTexCoord;
out vec4 outColor;

uniform sampler2D textureSampler;

void main() {
    outColor = texture(textureSampler, outTexCoord) * color;
}
