package show.twilight.finale.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.ChestBlock;
import show.twilight.finale.block.entity.TwilightTrapChestBlockEntity;

public class TwilightTrapChestRenderer implements BlockEntityRenderer<TwilightTrapChestBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest", "textures/entity/chest/twilight/single.png");
    private final ModelPart bottom, lid, lock;

    public TwilightTrapChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelLayers.CHEST);
        bottom = root.getChild("bottom");
        lid = root.getChild("lid");
        lock = root.getChild("lock");
    }

    @Override
    public void render(TwilightTrapChestBlockEntity chest, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float t = chest.openingTime(), age = chest.getLevel() == null ? 0 : chest.getLevel().getGameTime() + partial;
        if (t >= 0) t += partial;
        float growth = t < 0 ? 0 : (float) Mth.smoothstep(Mth.clamp((t - 30) / 65, 0, 1));
        float size = 1 + growth * 9, shake = t < 0 ? 0.018F : 0.035F + growth * 0.09F;
        pose.pushPose();
        pose.translate(0.5 + Math.sin(age * 2.5) * shake, 0, 0.5 + Math.cos(age * 3.3) * shake);
        pose.mulPose(Axis.YP.rotationDegrees(-chest.getBlockState().getValue(ChestBlock.FACING).toYRot()));
        pose.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(age * 1.8) * (t < 0 ? 1 : 3)));
        pose.scale(size, size, size);
        pose.translate(-0.5, 0, -0.5);
        float openness = t < 0 ? 0 : t < 25 ? 0.18F * Mth.sin(t / 25 * Mth.PI) : Mth.clamp((t - 90) / 25, 0, 1);
        lid.xRot = -openness * Mth.HALF_PI;
        lock.xRot = lid.xRot;
        var vertex = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        bottom.render(pose, vertex, light, overlay);
        lid.render(pose, vertex, light, overlay);
        lock.render(pose, vertex, light, overlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TwilightTrapChestBlockEntity chest) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(TwilightTrapChestBlockEntity chest) {
        return new net.minecraft.world.phys.AABB(chest.getBlockPos()).inflate(10);
    }
}
