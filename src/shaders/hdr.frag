#version 330

in vec2 TexCoords;

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 BloomColor;

uniform sampler2D tex;

void main() {
    vec3 color = texture(tex, TexCoords).rgb;
    color = pow(color, vec3(1.0/2.2));
    FragColor = vec4(min(color, 1), 1);
    BloomColor = vec4(max(color - 1, 0), 1);
}