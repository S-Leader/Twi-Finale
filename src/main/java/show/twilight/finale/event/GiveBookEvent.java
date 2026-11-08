package show.twilight.finale.event;

import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import twilightforest.util.landmarks.LandmarkUtil;
import twilightforest.world.components.structures.type.FinalCastleStructure;

import java.util.stream.IntStream;

@EventBusSubscriber(modid = "twilightforest_finale")
public final class GiveBookEvent {
    private static final String GIVEN = "tf_finale_has_received_castle_note";

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0 || player.getPersistentData().getBoolean(GIVEN))
            return;
        var pos = player.blockPosition();
        LandmarkUtil.locateNearestLandmarkStart(player.serverLevel(), SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())).filter(start -> start.getStructure() instanceof FinalCastleStructure && start.getBoundingBox().isInside(pos)).ifPresent(start -> {
            ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
            book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("Castle"), "Twilight Forest", 0, IntStream.rangeClosed(1, 4).mapToObj(i -> Filterable.passThrough((Component) Component.translatable("twilightforest.book.castle." + i))).toList(), true));
            player.getPersistentData().putBoolean(GIVEN, true);
            if (!player.getInventory().add(book)) player.drop(book, false);
            player.sendSystemMessage(Component.translatable("chat.twilightforest_finale.pick_note"));
        });
    }
}

