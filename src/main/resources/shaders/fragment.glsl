#version 330

uniform vec4 color;

in  vec2 outTexCoord;
in vec3 fragPos;
in vec3 outNormal;

out vec4 outColor;

uniform sampler2D textureSampler;
uniform vec3 lightPos;
uniform vec3 lightColor;

vec4 calcDiffuseComponent(vec3 normal, vec3 lightPos, vec3 lightColor, vec3 fragPos) {
    vec3 norm = normalize(outNormal);
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    return vec4(diffuse, 1.0);
}

void main() {
    vec4 diffuseComp = calcDiffuseComponent(outNormal, lightPos, fragPos, lightColor);
    outColor = texture(textureSampler, outTexCoord) * color + diffuseComponent;
}
