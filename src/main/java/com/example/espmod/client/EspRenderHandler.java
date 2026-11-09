package com.example.espmod.client;

import com.example.espmod.EspMod;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = EspMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EspRenderHandler {

    private static class EntityRenderInfo {
        Entity entity;
        double posX, posY, posZ;
        double halfWidth, height;
        double dist;
        float r, g, b;

        EntityRenderInfo(Entity entity, double posX, double posY, double posZ, double halfWidth, double height, double dist, float r, float g, float b) {
            this.entity = entity;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.halfWidth = halfWidth;
            this.height = height;
            this.dist = dist;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!ModConfig.espEnabled && !ModConfig.nameTagsEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        MatrixStack matrixStack = event.getMatrixStack();
        float partialTicks = event.getPartialTicks();
        Vector3d cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        List<EntityRenderInfo> targets = new ArrayList<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;

            // Target filters
            if (entity instanceof PlayerEntity) {
                if (!ModConfig.targetPlayers) continue;
            } else if (entity instanceof MonsterEntity) {
                if (!ModConfig.targetMonsters) continue;
            } else if (entity instanceof AnimalEntity) {
                if (!ModConfig.targetAnimals) continue;
            } else {
                if (!ModConfig.targetMisc) continue;
            }

            // Interpolated position
            double posX = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
            double posY = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
            double posZ = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());

            double dist = cameraPos.distanceTo(new Vector3d(posX, posY, posZ));
            if (dist > ModConfig.maxDistance) continue;

            // INTRINSIC base dimensions - unaffected by Hitbox Expander!
            double halfWidth = entity.getBbWidth() / 2.0;
            double height = entity.getBbHeight();

            // Color selection
            float r = 1.0f, g = 1.0f, b = 1.0f;
            if (entity instanceof PlayerEntity) {
                r = 0.0f; g = 0.7f; b = 1.0f; // Cyan
            } else if (entity instanceof MonsterEntity) {
                r = 1.0f; g = 0.2f; b = 0.2f; // Red
            } else if (entity instanceof AnimalEntity) {
                r = 0.2f; g = 1.0f; b = 0.2f; // Green
            } else if (entity instanceof MobEntity) {
                r = 1.0f; g = 0.8f; b = 0.2f; // Yellow
            } else {
                r = 0.8f; g = 0.8f; b = 0.8f; // Gray
            }

            targets.add(new EntityRenderInfo(entity, posX, posY, posZ, halfWidth, height, dist, r, g, b));
        }

        // ======================= 1. ESP BOXES & TRACERS =======================
        if (ModConfig.espEnabled && !targets.isEmpty()) {
            matrixStack.pushPose();
            matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(ModConfig.espLineWidth);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            Matrix4f matrix4f = matrixStack.last().pose();

            // Look vector for Tracers origin (starting from in front of camera)
            Vector3d viewVec = mc.player.getViewVector(partialTicks);
            float startX = (float) (cameraPos.x + viewVec.x * 0.5);
            float startY = (float) (cameraPos.y + viewVec.y * 0.5);
            float startZ = (float) (cameraPos.z + viewVec.z * 0.5);

            for (EntityRenderInfo info : targets) {
                // Render 3D Box around base dimensions (immune to Hitbox Expander)
                if (ModConfig.espBox) {
                    AxisAlignedBB box = new AxisAlignedBB(
                            info.posX - info.halfWidth, info.posY, info.posZ - info.halfWidth,
                            info.posX + info.halfWidth, info.posY + info.height, info.posZ + info.halfWidth
                    );
                    WorldRenderer.renderLineBox(
                            matrixStack,
                            buffer,
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            info.r, info.g, info.b, 1.0f
                    );
                }

                // Render Tracer line from crosshair to entity center
                if (ModConfig.espTracers) {
                    float targetX = (float) info.posX;
                    float targetY = (float) (info.posY + info.height / 2.0);
                    float targetZ = (float) info.posZ;

                    buffer.vertex(matrix4f, startX, startY, startZ).color(info.r, info.g, info.b, 0.7f).endVertex();
                    buffer.vertex(matrix4f, targetX, targetY, targetZ).color(info.r, info.g, info.b, 0.7f).endVertex();
                }
            }

            tessellator.end();

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();

            matrixStack.popPose();
        }

        // ======================= 2. NAMETAGS THROUGH WALLS =======================
        if (ModConfig.nameTagsEnabled && !targets.isEmpty()) {
            FontRenderer font = mc.font;

            for (EntityRenderInfo info : targets) {
                matrixStack.pushPose();
                matrixStack.translate(info.posX - cameraPos.x, info.posY - cameraPos.y + info.height + 0.45, info.posZ - cameraPos.z);
                matrixStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

                float scale = (float) Math.max(0.022D, info.dist * 0.0032D);
                matrixStack.scale(-scale, -scale, scale);

                // Build label string
                StringBuilder sb = new StringBuilder();
                sb.append(info.entity.getDisplayName().getString());

                if (ModConfig.nameTagsDistance) {
                    sb.append(" [").append((int) info.dist).append("m]");
                }

                if (ModConfig.nameTagsHealth && info.entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) info.entity;
                    int hp = (int) Math.ceil(living.getHealth());
                    int maxHp = (int) Math.ceil(living.getMaxHealth());
                    sb.append(" ").append(hp).append("/").append(maxHp).append(" HP");
                }

                String tagText = sb.toString();
                float width = font.width(tagText);
                float halfW = width / 2.0F;

                // DISABLE DEPTH TEST COMPLETELY for 100% see-through NameTags
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                // Draw translucent dark background box behind text
                AbstractGui.fill(matrixStack, (int) (-halfW - 3), -2, (int) (halfW + 3), 10, 0x99000000);

                // Draw colored text with shadow
                int textColor = 0xFFFFFFFF;
                if (info.entity instanceof PlayerEntity) {
                    textColor = 0xFF00E5FF; // Cyan
                } else if (info.entity instanceof MonsterEntity) {
                    textColor = 0xFFFF4444; // Red
                } else if (info.entity instanceof AnimalEntity) {
                    textColor = 0xFF55FF55; // Green
                }

                font.drawShadow(matrixStack, tagText, -halfW, 0, textColor);

                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
                RenderSystem.disableBlend();

                matrixStack.popPose();
            }
        }
    }
}
