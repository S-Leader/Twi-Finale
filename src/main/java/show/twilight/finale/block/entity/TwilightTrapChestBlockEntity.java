package show.twilight.finale.block.entity;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import show.twilight.finale.entity.*;
import show.twilight.finale.init.*;
import twilightforest.init.TFParticleType;
import java.util.UUID;

public class TwilightTrapChestBlockEntity extends ChestBlockEntity {
    public static final int OPEN_DURATION=160;
    private UUID hierophant,opener,monstrosity=UUID.randomUUID();
    private long openedAt=-1;
    public TwilightTrapChestBlockEntity(BlockPos pos,BlockState state) { super(ModBlocks.TWILIGHT_TRAP_CHEST_ENTITY.get(),pos,state); }
    public void bindHierophant(UUID owner) { hierophant=owner; setChanged(); }
    public UUID getMonstrosityUUID() { return monstrosity; }
    public int openingTime() { return openedAt<0 || level==null?-1:(int)Math.max(0,level.getGameTime()-openedAt); }
    public void open(Player player) {
        if (openedAt>=0 || level==null) return;
        openedAt=level.getGameTime(); opener=player.getUUID(); setChanged();
        level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),Block.UPDATE_CLIENTS);
        level.playSound(null,worldPosition,SoundEvents.CHEST_OPEN,SoundSource.BLOCKS,2,0.65F);
    }
    public static void tick(Level level,BlockPos pos,BlockState state,TwilightTrapChestBlockEntity chest) {
        if (!(level instanceof ServerLevel server)) return;
        int t=chest.openingTime();
        if (t<0) return;
        Vec3 centre=Vec3.atBottomCenterOf(pos);
        if (t%15==0) server.playSound(null,pos,SoundEvents.CHEST_LOCKED,SoundSource.BLOCKS,2,0.5F+t/160F);
        if (t>=70) {
            double spread=0.2+Math.min(t-70,70)/20D;
            server.sendParticles(TFParticleType.ANNIHILATE.get(),centre.x,centre.y+2,centre.z,8+t/8,spread,1.5,spread,0.08);
            server.sendParticles(TFParticleType.HUGE_SMOKE.get(),centre.x,centre.y+2,centre.z,4+t/12,spread,1.5,spread,0.05);
        }
        if (t<OPEN_DURATION) return;
        if (chest.hierophant!=null && !(server.getEntity(chest.hierophant) instanceof TheTwilightHierophantEntity)) return;
        if (server.getEntity(chest.monstrosity)==null) {
            CastleMonstrosityEntity beast=ModEntity.CASTLE_MONSTROSITY.get().create(server);
            if (beast==null) return;
            beast.setUUID(chest.monstrosity);
            beast.moveTo(centre.x,centre.y,centre.z,state.getValue(net.minecraft.world.level.block.ChestBlock.FACING).toYRot(),0);
            if (chest.hierophant!=null) beast.bindHierophant(chest.hierophant);
            if (chest.opener!=null && server.getPlayerByUUID(chest.opener) instanceof Player player) beast.setTarget(player);
            beast.activate();
            if (!server.addFreshEntity(beast)) return;
        }
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,centre.x,centre.y+2,centre.z,10,3,2,3,0);
        server.playSound(null,pos,SoundEvents.GENERIC_EXPLODE.value(),SoundSource.HOSTILE,6,0.5F);
        server.removeBlock(pos,false);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider provider) {
        super.saveAdditional(tag,provider);
        if (hierophant!=null) tag.putUUID("TrapHierophant",hierophant);
        if (opener!=null) tag.putUUID("TrapOpener",opener);
        tag.putUUID("TrapMonstrosity",monstrosity); tag.putLong("TrapOpenedAt",openedAt);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider provider) {
        super.loadAdditional(tag,provider);
        hierophant=tag.hasUUID("TrapHierophant")?tag.getUUID("TrapHierophant"):null;
        opener=tag.hasUUID("TrapOpener")?tag.getUUID("TrapOpener"):null;
        if (tag.hasUUID("TrapMonstrosity")) monstrosity=tag.getUUID("TrapMonstrosity");
        openedAt=tag.contains("TrapOpenedAt")?tag.getLong("TrapOpenedAt"):-1;
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) { return saveWithoutMetadata(provider); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
