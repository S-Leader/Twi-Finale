package show.twilight.finale.client.layer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.ClientHooks;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.client.model.TheTwilightHierophantModel;
import show.twilight.finale.entity.TheTwilightHierophantEntity;

import java.util.Map;

@OnlyIn(value = Dist.CLIENT)
public class HierophantArmorLayer<T extends TheTwilightHierophantEntity, M extends TheTwilightHierophantModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
    private final A model;
    private final ArmorModelPart modelPart;
    private final EquipmentSlot slot;
    private final boolean innerModel;
    private final TextureAtlas armorTrimAtlas;

    public HierophantArmorLayer(RenderLayerParent<T, M> renderer, A model, ArmorModelPart modelPart, boolean innerModel, EquipmentSlot slot, ModelManager manager) {
        super(renderer);
        this.model = model;
        this.modelPart = modelPart;
        this.innerModel = innerModel;
        this.slot = slot;
        this.armorTrimAtlas = manager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    public void render(PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        poseStack.pushPose();
        this.getParentModel().translateArmor(this.modelPart, poseStack, this.innerModel);
        this.renderArmorPiece(poseStack, bufferSource, entity, this.slot, packedLight, this.model);
        poseStack.popPose();
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot slot, int light, A model) {
        ArmorItem armorItem;
        ItemStack stack = entity.getItemBySlot(slot);
        Item item = stack.getItem();
        if (item instanceof ArmorItem && (armorItem = (ArmorItem) item).getEquipmentSlot() == slot) {
            this.getParentModel().copyPropertiesTo(model);
            this.setPartVisibility(model, this.modelPart);
            Model armorModel = this.getArmorModelHook(entity, stack, slot, model);
            int dye = net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack, -1);
            for (ArmorMaterial.Layer layer : armorItem.getMaterial().value().layers()) {
                int tint = layer.dyeable() ? dye : -1;
                this.renderModel(poseStack, bufferSource, light, armorModel, ((tint >>> 16) & 255) / 255F, ((tint >>> 8) & 255) / 255F, (tint & 255) / 255F, ClientHooks.getArmorTexture(entity, stack, layer, this.innerModel, slot));
            }
            ArmorTrim trim = stack.get(net.minecraft.core.component.DataComponents.TRIM);
            if (trim != null)
                this.renderTrim(armorItem.getMaterial(), poseStack, bufferSource, light, trim, armorModel, this.innerModel);
            if (stack.hasFoil()) {
                this.renderGlint(poseStack, bufferSource, light, armorModel);
            }
        }
    }

    protected void setPartVisibility(A model, ArmorModelPart part) {
        model.setAllVisible(false);
        switch (part) {
            case HEAD: {
                model.head.visible = true;
                model.hat.visible = true;
                break;
            }
            case BODY: {
                model.body.visible = true;
                break;
            }
            case RIGHT_ARM: {
                model.rightArm.visible = true;
                break;
            }
            case LEFT_ARM: {
                model.leftArm.visible = true;
                break;
            }
            case RIGHT_LEG: {
                model.rightLeg.visible = true;
                break;
            }
            case LEFT_LEG: {
                model.leftLeg.visible = true;
            }
        }
    }

    private void renderModel(PoseStack stack, MultiBufferSource bufferSource, int light, Model model, float red, float green, float blue, ResourceLocation texture) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(texture));
        model.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1.0f, red, green, blue));
    }

    private void renderTrim(net.minecraft.core.Holder<ArmorMaterial> material, PoseStack stack, MultiBufferSource bufferSource, int light, ArmorTrim trim, Model model, boolean innerModel) {
        TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(innerModel ? trim.innerTexture(material) : trim.outerTexture(material));
        VertexConsumer consumer = sprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
        model.renderToBuffer(stack, consumer, light, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
    }

    private void renderGlint(PoseStack stack, MultiBufferSource bufferSource, int light, Model model) {
        model.renderToBuffer(stack, bufferSource.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
    }

    protected Model getArmorModelHook(T entity, ItemStack stack, EquipmentSlot slot, A model) {
        return ClientHooks.getArmorModel(entity, stack, slot, model);
    }

    public static <T extends TheTwilightHierophantEntity, M extends TheTwilightHierophantModel<T>> void addCustomArmorLayers(LivingEntityRenderer<T, M> renderer, EntityRendererProvider.Context context, ModelLayerLocation innerLayer, ModelLayerLocation outerLayer) {
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(innerLayer)), ArmorModelPart.BODY, true, EquipmentSlot.LEGS, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(innerLayer)), ArmorModelPart.RIGHT_LEG, true, EquipmentSlot.LEGS, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(innerLayer)), ArmorModelPart.LEFT_LEG, true, EquipmentSlot.LEGS, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(outerLayer)), ArmorModelPart.HEAD, false, EquipmentSlot.HEAD, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(outerLayer)), ArmorModelPart.BODY, false, EquipmentSlot.CHEST, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(outerLayer)), ArmorModelPart.RIGHT_ARM, false, EquipmentSlot.CHEST, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(outerLayer)), ArmorModelPart.LEFT_ARM, false, EquipmentSlot.CHEST, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(outerLayer)), ArmorModelPart.RIGHT_LEG, false, EquipmentSlot.FEET, context.getModelManager()));
        renderer.addLayer(new HierophantArmorLayer<>(renderer, new HumanoidModel<>(context.bakeLayer(outerLayer)), ArmorModelPart.LEFT_LEG, false, EquipmentSlot.FEET, context.getModelManager()));
    }

    public enum ArmorModelPart {
        HEAD, BODY, RIGHT_ARM, LEFT_ARM, RIGHT_LEG, LEFT_LEG

    }
}

