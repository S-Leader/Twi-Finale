package show.twilight.finale.world.castle;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import show.twilight.finale.init.ModBlocks;

public final class FinaleCastleTemplatePiece extends TemplateStructurePiece {
    public FinaleCastleTemplatePiece(StructureTemplateManager manager, String name, BlockPos origin, Rotation rotation, Mirror mirror) {
        super(FinaleCastlePieces.INTERIOR.get(), 10, manager,
                ResourceLocation.fromNamespaceAndPath("twilightforest_finale", "final_castle/" + name),
                "final_castle/" + name, settings(rotation, mirror, name.startsWith("forge_room_") || name.startsWith("basement_decor_")), origin);
    }

    public FinaleCastleTemplatePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(FinaleCastlePieces.INTERIOR.get(), tag, context.structureTemplateManager(),
                id -> settings(Rotation.valueOf(tag.getString("FinaleRotation")), Mirror.valueOf(tag.getString("FinaleMirror")), id.getPath().startsWith("final_castle/forge_room_") || id.getPath().startsWith("final_castle/basement_decor_")));
    }

    private static StructurePlaceSettings settings(Rotation rotation, Mirror mirror, boolean dryForge) {
        var settings = new StructurePlaceSettings().setRotation(rotation).setMirror(mirror)
                .setIgnoreEntities(false).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        if (dryForge) settings.setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING);

        if (dryForge) settings.addProcessor(BasementBossProcessor.INSTANCE);
        else
            settings.addProcessor(new RuleProcessor(List.of(
                    new ProcessorRule(
                            new BlockMatchTest(ModBlocks.TWILIGHT_HIEROPHANT_BOSS_SPAWNER.get()),
                            AlwaysTrueTest.INSTANCE,
                            Blocks.AIR.defaultBlockState()))));
        return settings;
    }

    @Override
    protected ResourceLocation makeTemplateLocation() {
        return ResourceLocation.fromNamespaceAndPath("twilightforest_finale", templateName);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("FinaleRotation", placeSettings.getRotation().name());
        tag.putString("FinaleMirror", placeSettings.getMirror().name());
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos position, ServerLevelAccessor level, RandomSource random, BoundingBox bounds) {
    }
}
