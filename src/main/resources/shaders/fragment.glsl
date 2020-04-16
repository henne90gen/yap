#version 330

in vec2 outTexCoord;
in vec3 fragPos;
in vec3 outNormal;

uniform vec4 color;
uniform sampler2D textureSampler;
uniform vec3 lightPos;
uniform vec3 lightColor;

out vec4 outColor;

vec4 calcDiffuseComponent(vec3 normal, vec3 lightPos, vec3 lightColor, vec3 fragPos) {
    vec3 norm = normalize(normal);
    vec3 lightDir = normalize(lightPos - fragPos);
    // this fixes ambient lighting but breaks the fps debug rendering although the later
    // one should be influenced by ambient lighting at all...
    float diff = dot(norm, lightDir); //float diff = sign(dot(norm, lightDir));
    vec3 diffuse = diff * lightColor;
    return vec4(diffuse, 1.0);
}

void main() {
    vec4 diffuseComponent = calcDiffuseComponent(outNormal, lightPos, lightColor, fragPos);
    outColor = texture(textureSampler, outTexCoord) * color + diffuseComponent;
}
