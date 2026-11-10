package show.twilight.finale.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
    private final ModelPart right_shin;
    private final ModelPart left_shin;
    private final boolean slim;
    private int swordComboStage;
    private float swordComboTime, swordFlip, meteorSwordRaise;
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
        this.right_shin = this.right_leg.getChild("right_shin");
        this.left_shin = this.left_leg.getChild("left_shin");
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
        addJointedLeg(entity, "right", -1.9F, -2.1F, 0, 16, true, deformation);
        addJointedLeg(entity, "left", 1.9F, -1.9F, 16, 48, false, deformation);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static void addJointedLeg(PartDefinition entity, String side, float hipX, float boxX,
                                      int skinU, int skinV, boolean mirrorOuter, float deformation) {
        // Keep the existing hip names for walking/keyframes. In the neutral
        // pose the two six-pixel segments still form the original straight leg;
        // their side UVs continue at the exact original skin/robe texture rows.
        PartDefinition thigh = entity.addOrReplaceChild(side + "_leg",
                legSegment(boxX, skinU, skinV, 48, mirrorOuter, deformation), PartPose.offset(hipX, 6, 0));
        thigh.addOrReplaceChild(side + "_shin",
                legSegment(boxX, skinU, skinV + 6, 54, mirrorOuter, deformation), PartPose.offset(0, 6, 0));
    }

    private static CubeListBuilder legSegment(float boxX, int skinU, int skinV, int outerV,
                                              boolean mirrorOuter, float deformation) {
        return CubeListBuilder.create().texOffs(skinU, skinV)
                .addBox(boxX, 0, -2, 4, 6, 4, new CubeDeformation(deformation))
                .texOffs(0, outerV).mirror(mirrorOuter)
                .addBox(boxX, 0, -2, 4, 6, 4, new CubeDeformation(deformation + 0.25F));
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
        if (entity.getAction() == TheTwilightHierophantEntity.IDLE_ACTION || entity.getAction() == TheTwilightHierophantEntity.SHIELD_CAST)
            this.animateWalk(TheTwilightHierophantAnimation.WALK, limbSwing, limbSwingAmount, 1.5f, 2.0f);
    }

    @Override
    public void setupAnim(T boss, float swing, float amount, float age, float yaw, float pitch) {
        super.setupAnim(boss, swing, amount, age, yaw, pitch);
        int action = boss.getAction();
        float t = boss.getActionTime() + Mth.clamp(age - boss.tickCount, 0, 1);
        swordComboStage = boss.getSwordComboStage();
        swordComboTime = boss.getSwordComboTime() + Mth.clamp(age - boss.tickCount, 0, 1);
        swordFlip = 0;
        meteorSwordRaise = 0;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot += pitch * Mth.DEG_TO_RAD;
        if (action == TheTwilightHierophantEntity.IDLE_ACTION) {
            blendActionChange(boss, action, age);
            return;
        }
        var restPose = captureShadowPose();
        float weight = motionWindow(t, TheTwilightHierophantEntity.getActionDuration(action), 12, 20);
        switch (action) {
            case TheTwilightHierophantEntity.INTRO -> {
                float wand = smooth(t / 24F) * (1 - smooth((t - 82) / 18F));
                left_arm.xRot = right_arm.xRot = -1.5F * wand;
                left_arm.yRot = 0.3F * wand;
                right_arm.yRot = -0.3F * wand;
                weight = 1;
            }
            case TheTwilightHierophantEntity.SHIELD_CAST -> {
                float lastShot = 30 + (TheTwilightHierophantEntity.SHIELD_BURST_SHOTS - 1) * TheTwilightHierophantEntity.SHIELD_BURST_INTERVAL;
                float raise = smooth((t - 10) / 15F) * (1 - smooth((t - lastShot) / 12F));
                ModelPart casting = boss.getMainArm() == HumanoidArm.LEFT ? left_arm : right_arm;
                casting.xRot = Mth.lerp(raise, casting.xRot, -Mth.HALF_PI);
                casting.yRot = casting.zRot = 0;
                weight = 1;
            }
            case TheTwilightHierophantEntity.REBIRTH -> {
                float reach = smooth((t - 60) / 30F);
                ModelPart swordArm = boss.getMainArm() == HumanoidArm.LEFT ? left_arm : right_arm;
                swordArm.xRot = -1.3F * reach;
                left_arm.zRot = -0.35F;
                right_arm.zRot = 0.35F;
                body.xRot = -0.12F;
                left_leg.xRot = right_leg.xRot = 0.1F;
                weight = smooth(t / 12F);
            }
            case TheTwilightHierophantEntity.METEORS -> {
                boolean left = boss.getMainArm() == HumanoidArm.LEFT;
                ModelPart swordArm = left ? left_arm : right_arm;
                ModelPart freeArm = left ? right_arm : left_arm;
                float raise = smooth((t - 4) / 12F) * (1 - smooth((t - boss.getMeteorRainEndTick() - 10) / 18F));
                meteorSwordRaise = raise;
                float sinceFirst = t - TheTwilightHierophantEntity.METEOR_WINDUP_TICKS;
                float pulse = sinceFirst >= 0 && t <= boss.getMeteorRainEndTick() + 4
                        ? 1 - smooth((sinceFirst % boss.getMeteorInterval()) / 4F) : 0;
                // Castle Crashers' Demon Sword phase: a quick airborne lift,
                // one arm extended almost vertically, free arm trailing and
                // bent, asymmetric legs. Each summon gives the raised arm a pulse.
                swordArm.xRot = Mth.lerp(raise, swordArm.xRot, -3.12F + pulse * 0.12F);
                swordArm.yRot = 0;
                swordArm.zRot = (left ? 0.16F : -0.16F) * raise;
                freeArm.xRot = 0.28F * raise;
                freeArm.yRot = 0;
                freeArm.zRot = (left ? 0.3F : -0.3F) * raise;
                body.xRot = -0.14F * raise;
                body.yRot = (left ? 0.08F : -0.08F) * raise;
                body.zRot = (left ? 0.05F : -0.05F) * raise;
                head.xRot = -body.xRot;
                head.yRot = yaw * Mth.DEG_TO_RAD;
                left_leg.xRot = 0.4F * raise;
                right_leg.xRot = -0.12F * raise;
                left_shin.xRot = 0.5F * raise;
                right_shin.xRot = 0.35F * raise;
                weight = 1;
            }
            case TheTwilightHierophantEntity.FIERY_QUAKE -> {
                float local = swordComboTime;
                float kneel = 0;
                swordFlip = switch (swordComboStage) {
                    case TheTwilightHierophantEntity.SWORD_DIVE -> Mth.PI * smooth(local / 2F);
                    case TheTwilightHierophantEntity.SWORD_SMOKE -> Mth.PI;
                    case TheTwilightHierophantEntity.SWORD_PLANTED -> Mth.PI * (1 + smooth(local / 6F));
                    default -> 0;
                };
                if (swordComboStage == TheTwilightHierophantEntity.SWORD_PLANTED) kneel = smooth(local / 6F);
                if (swordComboStage == TheTwilightHierophantEntity.SWORD_FINISH) kneel = 1 - smooth(local / 12F);
                // Hammer-of-Justice down-swing reference: hold the weapon high,
                // tuck the legs and somersault into an inverted downward strike.
                // Keep that impact pose while dissolving: no rebound/unflip
                // between hits. Reset to the overhead pose during hidden travel.
                entity.xRot = Mth.wrapDegrees(swordFlip * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
                entity.y += 4 * kneel;
                body.xRot = Mth.lerp(kneel, -0.12F, 0.15F);
                body.yRot = body.zRot = 0;
                left_arm.xRot = right_arm.xRot = -3.0F;
                left_arm.yRot = 0.18F;
                right_arm.yRot = -0.18F;
                left_arm.zRot = -0.1F;
                right_arm.zRot = 0.1F;
                left_leg.xRot = right_leg.xRot = -1.0F;
                left_shin.xRot = right_shin.xRot = 1.5F;
                left_leg.yRot = right_leg.yRot = left_leg.zRot = right_leg.zRot = 0;
                head.xRot = 0.1F;
                if (swordComboStage == TheTwilightHierophantEntity.SWORD_PLANTED
                        || swordComboStage == TheTwilightHierophantEntity.SWORD_FINISH) {
                    // Fifth impact plants the blade immediately. Unfold into the
                    // existing single-knee stance while the one-second fuse runs.
                    ModelPart kneeling = boss.getMainArm() == HumanoidArm.LEFT ? left_leg : right_leg;
                    ModelPart kneelingShin = boss.getMainArm() == HumanoidArm.LEFT ? left_shin : right_shin;
                    ModelPart forward = boss.getMainArm() == HumanoidArm.LEFT ? right_leg : left_leg;
                    ModelPart forwardShin = boss.getMainArm() == HumanoidArm.LEFT ? right_shin : left_shin;
                    kneeling.xRot = Mth.lerp(kneel, -1, 0);
                    kneelingShin.xRot = Mth.lerp(kneel, 1.5F, Mth.HALF_PI);
                    forward.xRot = Mth.lerp(kneel, -1, -(float) Math.acos(1 / 3F));
                    forwardShin.xRot = Mth.lerp(kneel, 1.5F, (float) Math.acos(1 / 3F));
                    PoseStack torso = new PoseStack();
                    entity.translateAndRotate(torso);
                    body.translateAndRotate(torso);
                    Matrix4f inverse = new Matrix4f(torso.last().pose()).invert();
                    aimHandAt(left_arm, inverse, 1.2F, 14, -6);
                    aimHandAt(right_arm, inverse, -1.2F, 14, -6);
                    head.xRot = pitch * Mth.DEG_TO_RAD - body.xRot;
                }
                weight = smooth(t / 8F);
            }
            case TheTwilightHierophantEntity.FIERY_THRUST -> {
                float thrust = smooth((t - 18) / 4F);
                body.xRot = Mth.lerp(thrust, 0.1F, 0.7F);
                left_arm.xRot = right_arm.xRot = Mth.lerp(thrust, -1.9F, -Mth.HALF_PI);
                left_arm.yRot = Mth.lerp(thrust, 0.8F, 0.15F);
                right_arm.yRot = -left_arm.yRot;
                left_leg.xRot = -0.7F * thrust;
                right_leg.xRot = 0.8F * thrust;
                weight = motionWindow(t, TheTwilightHierophantEntity.FIERY_THRUST_DURATION, 8, 18);
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
                float release = smooth((t - TheTwilightHierophantEntity.AXE_WINDUP_TICKS + 8) / 8F);
                // Two-second overhead windup, then throw forward; no ground slam.
                body.xRot = Mth.lerp(release, -0.15F * lift, 0.2F);
                left_arm.xRot = right_arm.xRot = Mth.lerp(release, -2.9F * lift, -1.2F);
                left_arm.yRot = 0.45F * (1 - release);
                right_arm.yRot = -left_arm.yRot;
                left_arm.zRot = -0.25F * lift * (1 - release);
                right_arm.zRot = 0.25F * lift * (1 - release);
                left_leg.xRot = right_leg.xRot = 0;
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
                left_arm.zRot = -0.7F * (1 - smooth(t / TheTwilightHierophantEntity.BREAK_DURATION));
                right_arm.zRot = -left_arm.zRot;
                weight = 1;
            }
            case TheTwilightHierophantEntity.DEATH -> {
                float curl = smooth(t / 26F) * (1 - smooth((t - 36) / 5F));
                applyCurledPose(curl);
                float spread = smooth((t - 36) / 5F);
                left_arm.zRot = -1.5F * spread;
                right_arm.zRot = 1.5F * spread;
                left_arm.xRot = right_arm.xRot = -0.25F * spread;
                left_leg.zRot = -0.35F * spread;
                right_leg.zRot = 0.35F * spread;
                head.xRot = -0.45F * spread;
                entity.y -= spread * 2;
                weight = 1;
            }
        }
        blendFromRest(restPose, weight);
        blendActionChange(boss, action, age);
    }


    private static void aimHandAt(ModelPart arm, Matrix4f inverseTorso, float x, float y, float z) {
        Vector3f grip = inverseTorso.transformPosition(new Vector3f(x / 16, y / 16, z / 16));
        float dx = grip.x * 16 - arm.x, dy = grip.y * 16 - arm.y, dz = grip.z * 16 - arm.z;
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        arm.xRot = (float) Math.asin(Mth.clamp(dz / Math.max(0.001F, length), -1, 1));
        arm.yRot = 0;
        arm.zRot = (float) Math.atan2(-dx, dy);
    }

    public void translateToComboSword(PoseStack stack) {
        PoseStack hands = new PoseStack();
        entity.translateAndRotate(hands);
        body.translateAndRotate(hands);
        hands.pushPose();
        left_arm.translateAndRotate(hands);
        Vector3f grip = hands.last().pose().transformPosition(new Vector3f(0, 9 / 16F, 0));
        hands.popPose();
        right_arm.translateAndRotate(hands);
        grip.add(hands.last().pose().transformPosition(new Vector3f(0, 9 / 16F, 0))).mul(0.5F);
        root.translateAndRotate(stack);
        stack.translate(grip.x, grip.y, grip.z);
        if (swordComboStage == TheTwilightHierophantEntity.SWORD_PLANTED)
            stack.mulPose(Axis.ZP.rotationDegrees(45));
        else if (swordComboStage == TheTwilightHierophantEntity.SWORD_FINISH)
            stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(smooth(swordComboTime / 12F), 45, 225)));
        else {
            stack.mulPose(Axis.XP.rotation(swordFlip));
            stack.mulPose(Axis.ZP.rotationDegrees(225));
        }
        stack.scale(0.85F, 0.85F, 0.85F);
        stack.translate(0.25, 0.25, 0);
    }

    public void translateToRaisedSword(HumanoidArm arm, PoseStack stack) {
        PoseStack hand = new PoseStack();
        entity.translateAndRotate(hand);
        body.translateAndRotate(hand);
        (arm == HumanoidArm.LEFT ? left_arm : right_arm).translateAndRotate(hand);
        Vector3f grip = hand.last().pose().transformPosition(new Vector3f(0, 9 / 16F, 0));
        root.translateAndRotate(stack);
        stack.translate(grip.x, grip.y, grip.z);
        // Match the sampled raised hand but keep the blade vertical, independent
        // of the diagonal arm angle and native third-person item transforms.
        stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(meteorSwordRaise, 150, 225)));
        stack.scale(0.85F, 0.85F, 0.85F);
        stack.translate(0.25, 0.25, 0);
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

