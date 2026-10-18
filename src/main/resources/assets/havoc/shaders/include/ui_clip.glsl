/* Analytic shape-clip contract. Included only by HAVOC_ANALYTIC_CLIP variants. */
#moj_import <havoc:ui_geometry.glsl>

const int HAVOC_MAX_ANALYTIC_CLIPS = 4;
const float HAVOC_CLIP_RECT = 0.0;
const float HAVOC_CLIP_ROUNDED = 1.0;
const float HAVOC_CLIP_CHAMFER = 2.0;
const float HAVOC_CLIP_CIRCLE = 3.0;

layout (std140) uniform UIClip {
    vec4 uClipHeader; // x = primitive count
    vec4 uClipKinds;
    vec4 uClipBounds[HAVOC_MAX_ANALYTIC_CLIPS];
    vec4 uClipParams0[HAVOC_MAX_ANALYTIC_CLIPS];
    vec4 uClipParams1[HAVOC_MAX_ANALYTIC_CLIPS];
};

vec2 havocLogicalFragCoord() {
    vec2 framebuffer = max(uScreen.xy, vec2(1.0));
    vec2 logical = max(uScreen.zw, vec2(1.0));
    vec2 logicalScale = logical / framebuffer;
    return vec2(
        uLayer.x + gl_FragCoord.x * logicalScale.x,
        uLayer.y + uLayer.w - gl_FragCoord.y * logicalScale.y
    );
}

float havocRectSdf(vec2 p, vec2 halfSize) {
    vec2 q = abs(p) - halfSize;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0));
}

float havocClipPrimitiveDistance(int index, vec2 logicalPosition) {
    vec4 bounds = uClipBounds[index];
    vec2 size = max(bounds.zw, vec2(0.0001));
    vec2 halfSize = size * 0.5;
    vec2 p = logicalPosition - (bounds.xy + halfSize);
    float kind = uClipKinds[index];

    if (kind < 0.5) return havocRectSdf(p, halfSize);
    if (kind < 1.5) return roundedCornersSdf(p, halfSize, normalizeRadii(uClipParams0[index], size));
    if (kind < 2.5) {
        vec4 cutX = clamp(uClipParams0[index], 0.0, size.x);
        vec4 cutY = clamp(uClipParams1[index], 0.0, size.y);
        return chamferSdf(p, halfSize, cutX, cutY);
    }
    return length(p) - min(halfSize.x, halfSize.y);
}

float havocClipDistance(vec2 logicalPosition) {
    int count = clamp(int(uClipHeader.x + 0.5), 0, HAVOC_MAX_ANALYTIC_CLIPS);
    float distanceToIntersection = -1e20;
    for (int i = 0; i < HAVOC_MAX_ANALYTIC_CLIPS; i++) {
        if (i >= count) break;
        distanceToIntersection = max(distanceToIntersection,
                havocClipPrimitiveDistance(i, logicalPosition));
    }
    return distanceToIntersection;
}

float havocClipCoverage(float clipDistance, vec2 logicalScale) {
    float aa = max(max(logicalScale.x, logicalScale.y), max(fwidth(clipDistance) * 0.75, 0.0001));
    return clamp(0.5 - clipDistance / aa, 0.0, 1.0);
}
