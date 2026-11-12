package show.twilight.finale.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import show.twilight.finale.TwilightFinaleMod;
import show.twilight.finale.entity.CastleMonstrosityEntity;
import twilightforest.entity.monster.Adherent;

@EventBusSubscriber(modid = TwilightFinaleMod.MOD_ID)
public class MonstrosityEvents {
    @SubscribeEvent
    public static void onMinionDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Adherent minion) || !(minion.level() instanceof ServerLevel server)) return;
        var data = minion.getPersistentData();
        if (data.hasUUID(CastleMonstrosityEntity.SUMMONER_TAG) && server.getEntity(data.getUUID(CastleMonstrosityEntity.SUMMONER_TAG)) instanceof CastleMonstrosityEntity beast)
            beast.summonedAdherentDied(minion, event.getSource());
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile().level() instanceof ServerLevel server)) return;
        boolean water = event.getProjectile() instanceof ThrownPotion potion && potion.getItem().is(Items.SPLASH_POTION)
                && potion.getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
        boolean wind = event.getProjectile() instanceof AbstractWindCharge;
        if (!water && !wind) return;
        HitResult hit = event.getRayTraceResult();
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof CastleMonstrosityEntity beast)
            beast.interruptFlame();
        if (water)
            for (CastleMonstrosityEntity beast : server.getEntitiesOfClass(CastleMonstrosityEntity.class, new AABB(hit.getLocation(), hit.getLocation()).inflate(4, 2, 4))) {
                if (beast.getBoundingBox().distanceToSqr(hit.getLocation()) < 16) beast.interruptFlame();
            }
    }
}
