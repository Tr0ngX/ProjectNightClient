#version 330

// Text glow pass 2: vertical Gaussian + composite. Output = halo only (masked by 1 - original
// alpha) so glow never washes over the sharp text drawn on main. Un-premultiplies for the
// straight-alpha blitAndBlendToTexture that follows.

uniform sampler2D InSampler;
uniform sampler2D OriginalSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform TextGlowConfigV {
    vec4 Params;
};
#define Radius Params.x
#define Intensity Params.y

#define MAX_RADIUS 16

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 dir = vec2(0.0, 1.0 / InSize.y);
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
    vec4 blur = sum / weight;

    float alpha = clamp(blur.a * Intensity, 0.0, 1.0) * (1.0 - texture(OriginalSampler, texCoord).a);
    if (alpha <= 0.002) {
        fragColor = vec4(0.0);
        return;
    }
    fragColor = vec4(clamp(blur.rgb / max(blur.a, 0.0001), 0.0, 1.0), alpha);
}
