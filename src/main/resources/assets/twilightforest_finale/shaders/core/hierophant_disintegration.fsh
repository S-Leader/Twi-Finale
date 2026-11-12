#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    // Apply the skin's own transparency before the dissolve. No separate white geometry.
    if (color.a < 0.1) discard;
    vec2 pixel = floor(texCoord0 * vec2(textureSize(Sampler0, 0)));
    float noise = fract(sin(dot(pixel, vec2(12.9898, 78.233))) * 43758.5453);
    // Vertex alpha carries death progress, independently for each entity and robe pass.
    if (noise < vertexColor.a) discard;
    color *= vec4(vertexColor.rgb, 1.0) * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
