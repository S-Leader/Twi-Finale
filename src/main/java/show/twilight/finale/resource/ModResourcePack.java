package show.twilight.finale.resource;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;

@net.neoforged.fml.common.EventBusSubscriber(modid = "twilightforest_finale", bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public class ModResourcePack {
    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path modPath = FMLLoader.getLoadingModList().getModFileById("twilightforest_finale").getFile().findResource("packs/twilight_finale_pack");
            Pack pack = Pack.readMetaAndCreate(new net.minecraft.server.packs.PackLocationInfo("twilight_finale_pack", Component.literal("Modern Resources for TFFinale"), PackSource.BUILT_IN, java.util.Optional.empty()), new PathPackResources.PathResourcesSupplier(modPath), PackType.CLIENT_RESOURCES, new net.minecraft.server.packs.PackSelectionConfig(false, Pack.Position.TOP, false));
            event.addRepositorySource(consumer -> {
                if (pack != null) consumer.accept(pack);
            });
        }
    }
}

