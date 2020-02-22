#version 330

uniform vec4 color;

in  vec2 outTexCoord;
out vec4 outColor;

uniform sampler2D texture_sampler;

void main() {
    outColor = texture(texture_sampler, outTexCoord);;
}
