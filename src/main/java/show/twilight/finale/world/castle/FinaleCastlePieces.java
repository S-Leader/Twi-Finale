package show.twilight.finale.world.castle;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FinaleCastlePieces {
    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, "twilightforest_finale");
    public static final Supplier<StructureProcessorType<BasementBossProcessor>> BASEMENT_BOSS_PROCESSOR = PROCESSORS.register("basement_boss", () -> () -> BasementBossProcessor.CODEC);
    public static final DeferredRegister<StructurePieceType> TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, "twilightforest_finale");
    public static final Supplier<StructurePieceType> INTERIOR = TYPES.register("castle_interior", () -> FinaleCastleTemplatePiece::new);
    public static final Supplier<StructurePieceType> ROOM_LOAD = TYPES.register("castle_room_load", () -> FinalCastleRoomStructurePiece::new);
    public static final Supplier<StructurePieceType> PLAIN_TOWER = TYPES.register("castle_plain_treasure_tower", () -> FinalCastlePlainTowerPiece::new);
    public static final Supplier<StructurePieceType> FORGE_ACCESS = TYPES.register("forge_access", () -> FinalCastleForgeAccessComponent::new);
    public static final Supplier<StructurePieceType> REVIVED_BASEMENT = TYPES.register("revived_basement", () -> RevivedBasementPiece::new);
    public static final Supplier<StructurePieceType> REVIVED_ENTRY = TYPES.register("revived_basement_entry", () -> RevivedBasementEntry::new);
    public static final Supplier<StructurePieceType> REVIVED_CORRIDOR = TYPES.register("revived_basement_corridor", () -> RevivedBasementCorridor::new);
    public static final Supplier<StructurePieceType> REVIVED_DESCENT = TYPES.register("revived_basement_descent", () -> RevivedBasementDescent::new);
    public static final Supplier<StructurePieceType> TFFCMain = TYPES.register("tffcmain", () -> FinaleCastleMainComponent::new);
    public static final Supplier<StructurePieceType> TFFCStTo = TYPES.register("tffcstto", () -> FinalCastleStairTowerComponent::new);
    public static final Supplier<StructurePieceType> TFFCLaTo = TYPES.register("tffclato", () -> FinalCastleLargeTowerComponent::new);
    public static final Supplier<StructurePieceType> TFFCMur = TYPES.register("tffcmur", () -> FinalCastleMuralComponent::new);
    public static final Supplier<StructurePieceType> TFFCToF48 = TYPES.register("tffctof48", () -> FinalCastleFoundation48Component::new);
    public static final Supplier<StructurePieceType> TFFCRo48Cr = TYPES.register("tffcro48cr", () -> FinaleCastleRoof48CrenellatedComponent::new);
    public static final Supplier<StructurePieceType> TFFCBoGaz = TYPES.register("tffcbogaz", () -> FinalCastleBossGazeboComponent::new);
    public static final Supplier<StructurePieceType> TFFCSiTo = TYPES.register("tffcsito", () -> FinalCastleMazeTower13Component::new);
    public static final Supplier<StructurePieceType> TFFCDunSt = TYPES.register("tffcdunst", () -> FinalCastleDungeonStepsComponent::new);
    public static final Supplier<StructurePieceType> TFFCDunEn = TYPES.register("tffcdunen", () -> FinalCastleDungeonEntranceComponent::new);
    public static final Supplier<StructurePieceType> TFFCDunR31 = TYPES.register("tffcdunr31", () -> FinalCastleDungeonRoom31Component::new);
    public static final Supplier<StructurePieceType> TFFCDunEx = TYPES.register("tffcdunex", () -> FinalCastleDungeonExitComponent::new);
    public static final Supplier<StructurePieceType> TFFCDunBoR = TYPES.register("tffcdunbor", () -> FinalCastleDungeonForgeRoomComponent::new);
    public static final Supplier<StructurePieceType> TFFCRo9Cr = TYPES.register("tffcro9cr", () -> FinalCastleRoof9CrenellatedComponent::new);
    public static final Supplier<StructurePieceType> TFFCRo13Cr = TYPES.register("tffcro13cr", () -> FinalCastleRoof13CrenellatedComponent::new);
    public static final Supplier<StructurePieceType> TFFCRo13Con = TYPES.register("tffcro13con", () -> FinalCastleRoof13ConicalComponent::new);
    public static final Supplier<StructurePieceType> TFFCRo13Pk = TYPES.register("tffcro13pk", () -> FinalCastleRoof13PeakedComponent::new);
    public static final Supplier<StructurePieceType> TFFCEnTo = TYPES.register("tffcento", () -> FinalCastleEntranceTowerComponent::new);
    public static final Supplier<StructurePieceType> TFFCEnSiTo = TYPES.register("tffcensito", () -> FinalCastleEntranceSideTowerComponent::new);
    public static final Supplier<StructurePieceType> TFFCEnBoTo = TYPES.register("tffcenboto", () -> FinalCastleEntranceBottomTowerComponent::new);
    public static final Supplier<StructurePieceType> TFFCEnSt = TYPES.register("tffcenst", () -> FinalCastleEntranceStairsComponent::new);
    public static final Supplier<StructurePieceType> TFFCBelTo = TYPES.register("tffcbelto", () -> FinalCastleBellTower21Component::new);
    public static final Supplier<StructurePieceType> TFFCBri = TYPES.register("tffcbri", () -> FinalCastleBridgeComponent::new);
    public static final Supplier<StructurePieceType> TFFCToF13 = TYPES.register("tffctof13", () -> FinalCastleFoundation13Component::new);
    public static final Supplier<StructurePieceType> TFFCBeF21 = TYPES.register("tffcbef21", () -> FinalCastleBellFoundation21Component::new);
    public static final Supplier<StructurePieceType> TFFCFTh21 = TYPES.register("tffcfth21", () -> FinalCastleFoundation13ComponentThorns::new);
    public static final Supplier<StructurePieceType> TFFCDamT = TYPES.register("tffcdamt", () -> FinalCastleDamagedTowerComponent::new);
    public static final Supplier<StructurePieceType> TFFCWrT = TYPES.register("tffcwrt", () -> FinalCastleWreckedTowerComponent::new);
}
