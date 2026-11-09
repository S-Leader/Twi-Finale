package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import show.twilight.finale.client.animation.TheTwilightHierophantAnimation;
import show.twilight.finale.client.layer.HierophantArmorLayer;
import show.twilight.finale.entity.TheTwilightHierophantEntity;
import show.twilight.finale.runtime.client.model.AnimatableEntityModel;

@OnlyIn(value = Dist.CLIENT)
public class TheTwilightHierophantModel<T extends TheTwilightHierophantEntity>
        extends AnimatableEntityModel<T>
        implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "main");
    public static final ModelLayerLocation LAYER_LOCATION_SLIM = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant_slim"), "main");
    public static final ModelLayerLocation LAYER_LOCATION_OUTER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "outer");
    public static final ModelLayerLocation LAYER_LOCATION_OUTER_SLIM = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant_slim"), "outer");
    public static final ModelLayerLocation LAYER_LOCATION_O = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "armor_outer");
    public static final ModelLayerLocation LAYER_LOCATION_I = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "the_twilight_hierophant"), "armor_inner");
    private final ModelPart root;
    private final ModelPart entity;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart left_arm;
    private final ModelPart left_item;
    private final ModelPart right_arm;
    private final ModelPart right_item;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final boolean slim;
    private final java.util.Map<T, PoseTransition> transitions = new java.util.WeakHashMap<>();

    private static final class PoseTransition {
        int action;
        float changedAt, lastAge;
        java.util.List<ShadowBone> from, last;
    }

    public TheTwilightHierophantModel(ModelPart root, boolean slim) {
        this.root = root.getChild("root");
        this.slim = slim;
        this.entity = this.root.getChild("entity");
        this.body = this.entity.getChild("body");
        this.head = this.body.getChild("head");
        this.left_arm = this.body.getChild("left_arm");
        this.left_item = this.left_arm.getChild("left_item");
        this.right_arm = this.body.getChild("right_arm");
        this.right_item = this.right_arm.getChild("right_item");
        this.right_leg = this.entity.getChild("right_leg");
        this.left_leg = this.entity.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer(boolean slim, float deformation) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition entity = root.addOrReplaceChild("entity", CubeListBuilder.create(), PartPose.offset(0.0f, 6.0f, 0.0f));
        PartDefinition body = entity.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0f, -12.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)).texOffs(16, 16).addBox(-4.0f, -12.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(deformation)), PartPose.offset(0.0f, 6.0f, 0.0f));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(deformation)).texOffs(32, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(0.0f, -12.0f, 0.0f));
        PartDefinition left_arm;
        PartDefinition left_item;
        if (slim) {
            left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)).texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation)), PartPose.offset(5.0f, -10.0f, 0.0f));
            left_item = left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(deformation)), PartPose.offset(0.0f, 8.0f, 0.0f));
            PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation)).texOffs(40, 32).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(-5.0f, -10.0f, 0.0f));
            PartDefinition partDefinition = right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(deformation)), PartPose.offset(0.0f, 8.0f, 0.0f));
        } else {
            left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)).texOffs(32, 48).addBox(-1.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offset(5.0f, -10.0f, 0.0f));
            left_item = left_arm.addOrReplaceChild("left_item", CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 8.0f, 0.0f));
            PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(40, 32).addBox(-2.0f, -2.0f, -2.0f, 3.0f, 12.0f, 4.0f, new CubeDeformation(0.25f)).mirror(false), PartPose.offset(-5.0f, -10.0f, 0.0f));
            PartDefinition partDefinition = right_arm.addOrReplaceChild("right_item", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 8.0f, 0.0f));
        }
        PartDefinition right_leg = entity.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.1f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation)).texOffs(0, 48).mirror().addBox(-2.1f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(-1.9f, 6.0f, 0.0f));
        PartDefinition left_leg = entity.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-1.9f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation)).texOffs(0, 48).addBox(-1.9f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(deformation + 0.25f)), PartPose.offset(1.9f, 6.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public ModelPart root() {
        return this.root;
    }

    public ModelPart getHead() {
        return this.head;
    }

    /**
     * New skill poses must be frozen too, rather than replayed at the live cast time.
     */
    public record ShadowBone(PartPose pose, float xScale, float yScale, float zScale, boolean visible,
                             boolean skipDraw) {
    }

    public java.util.List<ShadowBone> captureShadowPose() {
        return root.getAllParts().map(p -> new ShadowBone(p.storePose(), p.xScale, p.yScale, p.zScale, p.visible, p.skipDraw)).toList();
    }

    public void applyShadowPose(java.util.List<ShadowBone> snapshot) {
        var parts = root.getAllParts().toList();
        if (parts.size() != snapshot.size()) throw new IllegalStateException("Mismatched Hierophant shadow skeleton");
        for (int i = 0; i < parts.size(); i++) {
            var part = parts.get(i);
            var saved = snapshot.get(i);
            part.loadPose(saved.pose());
            part.xScale = saved.xScale();
            part.yScale = saved.yScale();
            part.zScale = saved.zScale();
            part.visible = saved.visible();
            part.skipDraw = saved.skipDraw();
        }
    }

    public void setupWalkAnimation(T entity, float limbSwing, float limbSwingAmount) {
        if (entity.getAction() == TheTwilightHierophantEntity.IDLE_ACTION && !entity.hasShield())
            this.animateWalk(TheTwilightHierophantAnimation.WALK, limbSwing, limbSwingAmount, 1.5f, 2.0f);
    }

    @Override
    public void setupAnim(T boss, float swing, float amount, float age, float yaw, float pitch) {
        super.setupAnim(boss, swing, amount, age, yaw, pitch);
        int action = boss.getAction();
        float t = boss.getActionTime() + Mth.clamp(age - boss.tickCount, 0, 1);
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot += pitch * Mth.DEG_TO_RAD;
        // The first phase remains curled inside the shield, including recovery
        // between attacks. The intro starts upright and transitions INTO this pose.
        if (boss.hasShield() && action != TheTwilightHierophantEntity.INTRO && action != TheTwilightHierophantEntity.DEATH)
            applyCurledPose(1);
        if (action == TheTwilightHierophantEntity.IDLE_ACTION) {
            blendActionChange(boss, action, age);
            return;
        }
        var restPose = captureShadowPose();
        float weight = motionWindow(t, TheTwilightHierophantEntity.getActionDuration(action), 12, 20);
        switch (action) {
            case TheTwilightHierophantEntity.INTRO -> {
                float curl = smooth((t - 30) / 35F);
                applyCurledPose(curl);
                float wand = smooth((t - 18) / 20F) * (1 - smooth((t - 50) / 20F));
                boolean leftHanded = boss.getMainArm() == HumanoidArm.LEFT;
                ModelPart wandArm = leftHanded ? left_arm : right_arm;
                wandArm.xRot = Mth.lerp(wand, wandArm.xRot, -1.5F);
                wandArm.yRot = (leftHanded ? 1 : -1) * 0.25F * wand;
                weight = 1;
            }
            case TheTwilightHierophantEntity.SUMMON -> {
                left_arm.zRot = -1.25F;
                right_arm.zRot = 1.25F;
                left_arm.xRot = right_arm.xRot = -0.2F + Mth.sin(age * 0.15F) * 0.06F;
                left_arm.yRot = right_arm.yRot = 0;
                weight = motionWindow(t, TheTwilightHierophantEntity.getActionDuration(action), 20, 24);
            }
            case TheTwilightHierophantEntity.BARRAGE -> {
                left_arm.xRot = right_arm.xRot = -Mth.HALF_PI;
                left_arm.yRot = right_arm.yRot = left_arm.zRot = right_arm.zRot = 0;
                body.xRot = body.zRot = 0;
                weight = smooth(t / 12F); // Hold the firing pose until the accelerating barrage actually ends.
            }
            case TheTwilightHierophantEntity.SMOKE_MOVE -> {
                body.xRot += 0.7F;
                left_arm.xRot = right_arm.xRot = 0.8F;
                weight = motionWindow(t, TheTwilightHierophantEntity.getActionDuration(action), 8, 10);
            }
            case TheTwilightHierophantEntity.CHARGE -> {
                body.xRot -= boss.isShieldRushing() ? 0.12F : 0;
                weight = smooth(t / 12F);
            }
            case TheTwilightHierophantEntity.SHIELD_STUN -> {
                float dazed = motionWindow(t, boss.getShieldStopDuration(), 8, 12);
                entity.zRot = 0.25F * dazed;
                head.xRot += Mth.sin(age * 0.25F) * 0.08F * dazed;
                weight = 1;
            }
            case TheTwilightHierophantEntity.FAN -> {
                // ItemInHandLayer follows getMainArm(), including randomly
                // left-handed bosses. Animate that same arm, not always the right.
                boolean leftHanded = boss.getMainArm() == HumanoidArm.LEFT;
                ModelPart fanArm = leftHanded ? left_arm : right_arm;
                ModelPart freeArm = leftHanded ? right_arm : left_arm;
                fanArm.xRot = -Mth.HALF_PI;
                fanArm.yRot = (leftHanded ? -1 : 1) * Mth.lerp(smooth((t - 3) / 10F), -0.9F, 0.7F);
                fanArm.zRot = 0;
                freeArm.xRot = -0.4F;
                freeArm.yRot = freeArm.zRot = 0;
                body.xRot = -0.1F;
                weight = motionWindow(t, TheTwilightHierophantEntity.FAN_DURATION, 6, 6);
            }
            case TheTwilightHierophantEntity.ICE_BOMB -> {
                float reach = smooth(t / 18F);
                float release = smooth((t - 24) / 12F);
                left_arm.xRot = right_arm.xRot = Mth.lerp(release, -3.05F * reach, -1.15F);
                left_arm.yRot = right_arm.yRot = 0;
                left_arm.zRot = -0.35F * reach * (1 - release);
                right_arm.zRot = 0.35F * reach * (1 - release);
                body.xRot = Mth.lerp(release, -0.15F * reach, 0.2F);
                head.xRot = pitch * Mth.DEG_TO_RAD - body.xRot;
                weight = motionWindow(t, TheTwilightHierophantEntity.ICE_BOMB_DURATION, 8, 14);
            }
            case TheTwilightHierophantEntity.WEAPON_VOLLEY -> {
                if (boss.getWeaponCount() >= TheTwilightHierophantEntity.WEAPON_THROW_COUNT) {
                    float relax = smooth(boss.getWeaponThrowProgress(t));
                    left_arm.xRot = right_arm.xRot = Mth.lerp(relax, -1.3F, -0.25F);
                    left_arm.yRot = right_arm.yRot = left_arm.zRot = right_arm.zRot = 0;
                } else {
                    boolean useMain = boss.getWeaponCount() % 2 == 0;
                    boolean useLeft = (boss.getMainArm() == HumanoidArm.LEFT) == useMain;
                    ModelPart throwing = useLeft ? left_arm : right_arm;
                    ModelPart other = useLeft ? right_arm : left_arm;
                    float progress = boss.getWeaponThrowProgress(t);
                    float take = smooth(progress / 0.55F);
                    float toss = smooth((progress - 0.55F) / 0.45F);
                    throwing.xRot = Mth.lerp(toss, Mth.lerp(take, -0.35F, -3.05F), -1.3F);
                    throwing.yRot = (useLeft ? -1 : 1) * 0.25F * (1 - toss);
                    throwing.zRot = 0;
                    other.xRot = Mth.lerp(smooth(progress / 0.3F), -1.3F, -0.25F);
                    other.yRot = other.zRot = 0;
                    body.yRot = (useLeft ? 1 : -1) * 0.15F * Mth.sin(progress * Mth.PI);
                }
                head.yRot = yaw * Mth.DEG_TO_RAD - body.yRot;
                weight = smooth(t / 10F);
            }
            case TheTwilightHierophantEntity.AXE_SLAM -> {
                float lift = smooth(t / 20F);
                float slam = smooth((t - TheTwilightHierophantEntity.AXE_WINDUP_TICKS + 4) / 4F);
                // Lift and hold overhead, then bring the axe down at tick 40.
                body.xRot = Mth.lerp(slam, -0.15F * lift, 0.8F);
                left_arm.xRot = right_arm.xRot = Mth.lerp(slam, -2.9F * lift, -0.6F);
                left_arm.yRot = 0.45F;
                right_arm.yRot = -0.45F;
                left_arm.zRot = -0.25F * lift * (1 - slam);
                right_arm.zRot = 0.25F * lift * (1 - slam);
                left_leg.xRot = right_leg.xRot = -0.25F * slam;
                head.xRot = pitch * Mth.DEG_TO_RAD - body.xRot;
                weight = motionWindow(t, TheTwilightHierophantEntity.AXE_SLAM_DURATION, 8, 18);
            }
            case TheTwilightHierophantEntity.CHUCKLE -> {
                boolean left = boss.getMainArm() == HumanoidArm.LEFT;
                ModelPart mouthHand = left ? left_arm : right_arm;
                ModelPart resting = left ? right_arm : left_arm;
                body.xRot = 0.45F + Mth.sin(t * 1.5F) * 0.025F;
                mouthHand.xRot = -2.25F;
                mouthHand.yRot = left ? 0.65F : -0.65F;
                mouthHand.zRot = left ? -0.15F : 0.15F;
                resting.xRot = -0.4F;
                head.xRot = 0.25F;
                weight = motionWindow(t, TheTwilightHierophantEntity.CHUCKLE_DURATION, 8, 8);
            }
            case TheTwilightHierophantEntity.RETURN_FRAGMENTS, TheTwilightHierophantEntity.FORMATION -> {
                left_arm.zRot = -0.6F;
                right_arm.zRot = 0.6F;
                left_arm.xRot = right_arm.xRot = -1.1F;
            }
            case TheTwilightHierophantEntity.SHIELD_BREAK -> {
                applyCurledPose(1 - smooth(t / TheTwilightHierophantEntity.BREAK_DURATION));
                weight = 1;
            }
            case TheTwilightHierophantEntity.DEATH -> {
                float kneel = smooth((t - 35) / 35F);
                entity.y += kneel * 7;
                body.xRot = kneel * 0.7F;
                head.xRot = kneel * 0.7F;
                left_leg.xRot = right_leg.xRot = -kneel * 1.5F;
                left_arm.xRot = right_arm.xRot = -kneel * 0.35F;
                entity.zRot = Mth.sin(age * 1.8F) * (0.01F + Math.min(t, 55) / 700F) * (1 - kneel * 0.8F) * smooth(t / 12F);
                weight = 1;
            }
        }
        blendFromRest(restPose, weight);
        blendActionChange(boss, action, age);
    }

    private void applyCurledPose(float curl) {
        float knee = curl;
        body.xRot = 1.15F * curl;
        head.xRot = 0.8F * curl;
        left_leg.xRot = right_leg.xRot = -knee;
        left_arm.xRot = right_arm.xRot = -1.3F * curl;
        left_arm.yRot = right_arm.yRot = left_arm.zRot = right_arm.zRot = 0;

        // A floor-level crouch leaves the boss below its cube. Tuck the body
        // into the shield instead: place the rotated torso midpoint at the
        // collision-box centre. Model coordinates are 16 units/block, Y down;
        // LivingEntityRenderer's flipped model origin is 1.501 blocks above the feet.
        float torsoY = entity.y + body.y - 6 * Mth.cos(body.xRot);
        float torsoZ = entity.z + body.z - 6 * Mth.sin(body.xRot);
        float shieldCentreY = (1.501F - TheTwilightHierophantEntity.SHIELD_SIZE / 2) * 16;
        entity.y += (shieldCentreY - torsoY) * curl;
        entity.z -= torsoZ * curl;
    }

    private static float smooth(float progress) {
        float t = Mth.clamp(progress, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static float motionWindow(float time, float duration, float in, float out) {
        return smooth(time / in) * smooth((duration - time) / out);
    }

    private void blendFromRest(java.util.List<ShadowBone> rest, float weight) {
        var parts = root.getAllParts().toList();
        for (int i = 0; i < parts.size(); i++) {
            var part = parts.get(i);
            var pose = rest.get(i).pose();
            part.x = Mth.lerp(weight, pose.x, part.x);
            part.y = Mth.lerp(weight, pose.y, part.y);
            part.z = Mth.lerp(weight, pose.z, part.z);
            part.xRot = Mth.lerp(weight, pose.xRot, part.xRot);
            part.yRot = Mth.lerp(weight, pose.yRot, part.yRot);
            part.zRot = Mth.lerp(weight, pose.zRot, part.zRot);
        }
    }

    private void blendActionChange(T boss, int action, float age) {
        // Each model is shared by many entities: keep independent transitions,
        // and use weak keys so an unloaded boss is not retained by the renderer.
        PoseTransition transition = transitions.get(boss);
        if (transition == null || age < transition.lastAge || age - transition.lastAge > 6) {
            transition = new PoseTransition();
            transition.action = action;
            transitions.put(boss, transition);
        } else if (transition.action != action) {
            transition.from = transition.last;
            transition.changedAt = age;
            transition.action = action;
        }
        if (transition.from != null) {
            float weight = smooth((age - transition.changedAt) / 6F);
            blendFromRest(transition.from, weight);
            if (weight >= 1) transition.from = null;
        }
        transition.last = captureShadowPose();
        transition.lastAge = age;
    }

    public void translateToHand(@NotNull HumanoidArm arm, @NotNull PoseStack stack) {
        ModelPart armPart;
        this.root.translateAndRotate(stack);
        this.entity.translateAndRotate(stack);
        this.body.translateAndRotate(stack);
        ModelPart modelPart = armPart = arm == HumanoidArm.LEFT ? this.left_arm : this.right_arm;
        if (this.slim) {
            float f = 0.5f * (float) (arm == HumanoidArm.RIGHT ? 1 : -1);
            armPart.x += f;
            armPart.translateAndRotate(stack);
            armPart.x -= f;
        } else {
            armPart.translateAndRotate(stack);
        }
        ModelPart itemPart = arm == HumanoidArm.LEFT ? this.left_item : this.right_item;
        itemPart.translateAndRotate(stack);
        stack.translate(0.0, -0.5, 0.0);
    }

    public void translateArmor(HierophantArmorLayer.ArmorModelPart part, PoseStack stack, boolean innerModel) {
        this.root.translateAndRotate(stack);
        this.entity.translateAndRotate(stack);
        switch (part) {
            case HEAD: {
                this.body.translateAndRotate(stack);
                this.head.translateAndRotate(stack);
                break;
            }
            case BODY: {
                this.body.translateAndRotate(stack);
                stack.translate(0.0, -0.75, 0.0);
                break;
            }
            case LEFT_ARM: {
                this.body.translateAndRotate(stack);
                this.left_arm.translateAndRotate(stack);
                stack.translate(0.0, -0.125, 0.0);
                stack.translate(-0.275, 0.0, 0.0);
                break;
            }
            case RIGHT_ARM: {
                this.body.translateAndRotate(stack);
                this.right_arm.translateAndRotate(stack);
                stack.translate(0.0, -0.125, 0.0);
                stack.translate(0.275, 0.0, 0.0);
                break;
            }
            case LEFT_LEG: {
                this.left_leg.translateAndRotate(stack);
                stack.translate(0.0, -0.75, 0.0);
                stack.translate(-0.125, 0.0, 0.0);
                break;
            }
            case RIGHT_LEG: {
                this.right_leg.translateAndRotate(stack);
                stack.translate(0.0, -0.75, 0.0);
                stack.translate(0.125, 0.0, 0.0);
            }
        }
    }
}

