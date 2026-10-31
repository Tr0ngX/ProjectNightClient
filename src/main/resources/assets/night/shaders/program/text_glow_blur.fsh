#version 330

// Text glow pass 1: horizontal Gaussian over the premultiplied text mask (TextGlow capture).
// Whole-mask blur, so one cost per frame no matter how many strings glow.

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform TextGlowConfig {
    vec4 Params;
};
#define Radius Params.x

#define MAX_RADIUS 16

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 dir = vec2(1.0 / InSize.x, 0.0);
    float sigma = max(Radius * 0.5, 0.5);
    float k = -0.5 / (sigma * sigma);

    vec4 sum = texture(InSampler, texCoord);
    float weight = 1.0;
    for (int i = 1; i <= MAX_RADIUS; i++) {
        if (float(i) > Radius) break;
        float w = exp(float(i * i) * k);
        sum += (texture(InSampler, texCoord + dir * float(i)) + texture(InSampler, texCoord - dir * float(i))) * w;
        weight += 2.0 * w;
    }

    fragColor = sum / weight;
}
