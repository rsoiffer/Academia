#version 330 core

in vec3 FragPos;
in vec3 Normal;

layout (location = 0) out vec3 gPosition;
layout (location = 1) out vec3 gNormal;
layout (location = 2) out vec3 gAlbedo;
layout (location = 3) out vec3 gMRA;
layout (location = 4) out vec3 gEmissive;

// material parameters
uniform float additive;
uniform vec3 color;
uniform float metallic;
uniform float roughness;
uniform float ao;
uniform vec3 emissive;

void main()
{
    gPosition = FragPos * (1 - additive);
    gNormal = normalize(Normal) * (1 - additive);
    gAlbedo = color;
    gMRA = vec3(metallic, roughness, ao);
    gEmissive = emissive;
}
