/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
#version 130

uniform mat4 bindSpaceMat;
uniform mat4 modelViewProjMat;
uniform mat4 normalMat;

uniform int maxNumBones;

in vec4 position;
in vec4 normal;
in vec2 texCoord;

in vec4 boneIndices;
in vec4 boneWeights;

const int MAX_BONES = 100;
uniform mat4 boneMatrices[MAX_BONES];

out vec2 texCoordFrag;
out vec3 normalFrag;

uniform float C;
uniform float FC;
out float interpZ;

void main()
{
    vec4 animatedPos = vec4(0.0, 0.0, 0.0, 1.0);
    vec4 animatedNormal = vec4(0.0, 0.0, 0.0, 0.0);

    vec4 nor;
    nor.xyz = normalize(normal.xyz);
    nor.w = 0;

    vec4 bindSpacePos = bindSpaceMat * position;
    vec4 bindSpaceNor = bindSpaceMat * nor;

    for (int b = 0; b < maxNumBones; ++b)
    {
        int boneIndex = int(boneIndices[b]);
        mat4 boneMat = mat4(1.0);
        if (boneIndex != -1) {
            boneMat = boneMatrices[boneIndex];
        }

        vec4 partialPos = boneMat * bindSpacePos;
        float weight = boneWeights[b];
        animatedPos.xyz += (partialPos * weight).xyz;

        vec4 partialNormal = boneMatrices[boneIndex] * bindSpaceNor;
        animatedNormal.xyz += (partialNormal * weight).xyz;
    }
    if (maxNumBones == 0) {
        animatedPos = bindSpacePos;
        animatedNormal = bindSpaceNor;
    }

    gl_Position = modelViewProjMat * animatedPos;

    normalFrag = (normalMat * animatedNormal).xyz;

    texCoordFrag = texCoord;

    // Logarithmic depth buffer
    interpZ = gl_Position.w;
    float logIn = interpZ*C+1;
    float logVal = 0;
    // Linearize for negative values (
    if (logIn < 0) {
        logVal = interpZ*C;
    } else {
        logVal = log(logIn);
    }
    gl_Position.z = (2*logVal*FC - 1)*gl_Position.w;
}
