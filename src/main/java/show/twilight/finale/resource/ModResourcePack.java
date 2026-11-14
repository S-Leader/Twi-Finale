package show.twilight.finale.resource;

import java.nio.file.Path;
import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import show.twilight.finale.TwilightFinaleMod;

public class ModResourcePack {
    public static final String JAPPA_PACK_ID = TwilightFinaleMod.MOD_ID;

    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path modPath = FMLLoader.getLoadingModList().getModFileById(TwilightFinaleMod.MOD_ID).getFile().findResource("packs/twilight_finale_pack");
            Pack pack = Pack.readMetaAndCreate(new PackLocationInfo(JAPPA_PACK_ID, Component.literal("Modern Resources for TFFinale"), PackSource.BUILT_IN, Optional.empty()), new PathPackResources.PathResourcesSupplier(modPath), PackType.CLIENT_RESOURCES, new PackSelectionConfig(false, Pack.Position.TOP, false));
            event.addRepositorySource(consumer -> {
                if (pack != null) consumer.accept(pack);
            });
        }
    }
}

