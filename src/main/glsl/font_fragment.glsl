#version 330

uniform vec4 color;

in  vec2 outTexCoord;
out vec4 outColor;

uniform sampler2D textureSampler;

void main() {
    vec4 c = texture(textureSampler, outTexCoord);
    outColor = color * vec4(1.0, 1.0, 1.0, c.w);
}
