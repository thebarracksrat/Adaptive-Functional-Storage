package com.silentvector.adaptivefunctionalstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

final class EmissiveScreenRenderer {
    static void render(PoseStack pose, MultiBufferSource buffers, int overlay, Direction facing, ResourceLocation texture) {
        render(pose, buffers, overlay, facing, texture, new float[][]{{0, 0, 1, 1}});
    }

    static void render(PoseStack pose, MultiBufferSource buffers, int overlay, Direction facing, ResourceLocation texture, float[][] rectangles) {
        var consumer=buffers.getBuffer(RenderType.eyes(texture));
        pose.pushPose();
        pose.mulPose(createTransformMatrix(new Vector3f(0),new Vector3f(0,180,0),1));
        switch(facing){
            case NORTH->pose.mulPose(createTransformMatrix(new Vector3f(-1,0,0),new Vector3f(0),1));
            case EAST->pose.mulPose(createTransformMatrix(new Vector3f(-1,0,-1),new Vector3f(0,-90,0),1));
            case SOUTH->pose.mulPose(createTransformMatrix(new Vector3f(0,0,-1),new Vector3f(0,180,0),1));
            case WEST->pose.mulPose(createTransformMatrix(new Vector3f(0),new Vector3f(0,90,0),1));
            default->{pose.popPose();return;}
        }
        pose.translate(0,0,-0.034D);
        var matrix=pose.last().pose();
        for (float[] rectangle : rectangles) {
            float left=rectangle[0], bottom=rectangle[1], right=rectangle[2], top=rectangle[3];
            consumer.addVertex(matrix,left,top,0).setColor(255,255,255,255).setUv(0,0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0,0,-1);
            consumer.addVertex(matrix,right,top,0).setColor(255,255,255,255).setUv(1,0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0,0,-1);
            consumer.addVertex(matrix,right,bottom,0).setColor(255,255,255,255).setUv(1,1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0,0,-1);
            consumer.addVertex(matrix,left,bottom,0).setColor(255,255,255,255).setUv(0,1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(0,0,-1);
        }
        pose.popPose();
    }
    private EmissiveScreenRenderer(){}
}
