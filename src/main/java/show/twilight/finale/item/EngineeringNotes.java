package show.twilight.finale.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import show.twilight.finale.TwilightFinaleMod;

import java.util.ArrayList;
import java.util.List;

public final class EngineeringNotes {
    public static final String PREFIX = "book.twilightforest_finale.engineering_notes";
    // Keep the reserved discovery advancement ID so existing player progress survives.
    public static final net.minecraft.resources.ResourceLocation DISCOVERY = TwilightFinaleMod.prefix("found_purple_adherent");

    private EngineeringNotes() {
    }

    public static boolean hasDiscovered(ServerPlayer player) {
        var advancement = player.server.getAdvancements().get(DISCOVERY);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static String ending(int state) {
        return switch (state & 7) {
            case 1 -> "priest";
            case 2 -> "golem";
            case 3 -> "priest_golem";
            case 4 -> "hierophant";
            case 5 -> "hierophant_priest";
            case 6 -> "hierophant_golem";
            case 7 -> "all";
            default -> "";
        };
    }

    public static ItemStack create(int castleState) {
        List<Filterable<Component>> pages = new ArrayList<>();
        for (int page = 1; page <= 33; page++)
            pages.add(Filterable.passThrough(Component.translatable(PREFIX + ".page." + page)));
        String ending = ending(castleState);
        if (!ending.isEmpty()) pages.add(Filterable.passThrough(Component.translatable(PREFIX + ".ending." + ending)));
        Component title = Component.translatable(PREFIX + ".title");
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title.getString()),
                Component.translatable(PREFIX + ".author").getString(), 0, pages, true));
        // Component-valued names/pages remain localized by the reader's client.
        book.set(DataComponents.CUSTOM_NAME, title);
        return book;
    }
}
