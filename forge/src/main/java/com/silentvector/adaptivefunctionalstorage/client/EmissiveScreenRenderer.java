package com.silentvector.adaptivefunctionalstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

final class EmissiveScreenRenderer {
    static void render(PoseStack pose, MultiBufferSource buffers, int overlay, Direction facing, ResourceLocation texture) {
        render(pose, buffers, overlay, facing, texture, new float[][]{{0, 0, 1, 1}});
    }

    static void render(PoseStack pose, MultiBufferSource buffers, int overlay, Direction facing, ResourceLocation texture, float[][] rectangles) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.eyes(texture));
        pose.pushPose();
        pose.mulPoseMatrix(createTransformMatrix(new Vector3f(0), new Vector3f(0, 180, 0), 1));
        switch (facing) {
            case NORTH -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0), 1));
            case EAST -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(-1, 0, -1), new Vector3f(0, -90, 0), 1));
            case SOUTH -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
            case WEST -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(0), new Vector3f(0, 90, 0), 1));
            default -> {
                pose.popPose();
                return;
            }
        }
        pose.translate(0, 0, -0.034D);
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        for (float[] rectangle : rectangles) {
            float left = rectangle[0], bottom = rectangle[1], right = rectangle[2], top = rectangle[3];
            vertex(consumer, matrix, normal, left, top, 0, 0, overlay);
            vertex(consumer, matrix, normal, right, top, 1, 0, overlay);
            vertex(consumer, matrix, normal, right, bottom, 1, 1, overlay);
            vertex(consumer, matrix, normal, left, bottom, 0, 1, overlay);
        }
        pose.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float u, float v, int overlay) {
        consumer.vertex(matrix, x, y, 0)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 0, -1)
                .endVertex();
    }

    private EmissiveScreenRenderer() {
    }
}
