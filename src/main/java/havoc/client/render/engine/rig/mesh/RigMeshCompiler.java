/*
 * This file is part of the Havoc Client distribution.
 * Copyright (c) 2026 pivosos2007.
 *
 * Licensed under the GNU General Public License v3.0.
 */

package havoc.client.render.engine.rig.mesh;

import com.mojang.blaze3d.PrimitiveTopology;
import havoc.client.render.engine.rhi.HavocRhi;
import havoc.client.render.engine.rhi.GpuMeshHandle;
import havoc.client.render.engine.uniform.MeshBuilder;
import havoc.client.render.engine.vertex.HavocVertexFormats;

/**
 * Compiles immutable CPU rig geometry into Havoc's existing MeshBuilder/RHI path.
 */
public final class RigMeshCompiler {
    private RigMeshCompiler() {
    }

    public static MeshBuilder compile(RigMeshData data) {
        if (data == null) throw new IllegalArgumentException("Rig mesh data must not be null");
        if (data.vertexCount() <= 0 || data.indexCount() <= 0) {
            throw new IllegalArgumentException("Rig mesh data must contain renderable geometry");
        }

        MeshBuilder mesh = new MeshBuilder(
                HavocVertexFormats.RIG_POSITION_TEXTURE_NORMAL_COLOR_BONES_DEFORM,
                PrimitiveTopology.TRIANGLES,
                data.vertexCount(),
                data.indexCount()
        );
        boolean complete = false;
        try {
            mesh.beginLocal();
            int baseVertex = 0;
            for (int partIndex = 0; partIndex < data.partCount(); partIndex++) {
                RigMeshPart part = data.part(partIndex);
                RigVertex[] vertices = part.verticesRef();
                for (RigVertex vertex : vertices) {
                    writeVertex(mesh, vertex);
                }

                int[] indices = part.indicesRef();
                for (int i = 0; i < indices.length; i += 3) {
                    mesh.triangle(
                            baseVertex + indices[i],
                            baseVertex + indices[i + 1],
                            baseVertex + indices[i + 2]
                    );
                }
                baseVertex += vertices.length;
            }
            mesh.end();
            complete = true;
            return mesh;
        } finally {
            if (!complete) mesh.close();
        }
    }

    /** Uploads through the existing dynamic-mesh backend; returned ownership is defined by HavocRHI. */
    public static GpuMeshHandle upload(HavocRhi rhi, RigMeshData data) {
        if (rhi == null) throw new IllegalArgumentException("HavocRHI must not be null");
        try (MeshBuilder mesh = compile(data)) {
            return rhi.dynamicMeshes().upload(mesh);
        }
    }

    private static void writeVertex(MeshBuilder mesh, RigVertex vertex) {
        mesh.vec3(vertex.x(), vertex.y(), vertex.z());
        mesh.raw2(vertex.u(), vertex.v());
        mesh.vec3(vertex.normalX(), vertex.normalY(), vertex.normalZ());
        mesh.colorArgb(vertex.colorArgb());
        mesh.u8x4(
                RigVertexEncoding.encodeBoneIndex(vertex.bone0()),
                RigVertexEncoding.encodeBoneIndex(vertex.bone1()),
                RigVertexEncoding.encodeBoneIndex(vertex.bone2()),
                RigVertexEncoding.encodeBoneIndex(vertex.bone3())
        );
        mesh.unorm8x4(vertex.weight0(), vertex.weight1(), vertex.weight2(), vertex.weight3());
        mesh.vec4(vertex.deformU(), vertex.deformLateral(), vertex.deformDepth(), vertex.deformAux());
        mesh.uint(RigVertexEncoding.packDeformMeta(vertex.deformId(), vertex.deformFlags()));
        mesh.next();
    }
}
