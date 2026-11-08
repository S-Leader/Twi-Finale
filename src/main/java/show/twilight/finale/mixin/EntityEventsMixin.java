package show.twilight.finale.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import twilightforest.TwilightForestMod;
import twilightforest.events.EntityEvents;

@Mixin(value = {EntityEvents.class})
public class EntityEventsMixin {
    @Redirect(method = {"alertPlayerCastleIsWIP"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void redirectWIPMessage(Player player, Component originalComponent, AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getAdvancement().id().equals(TwilightForestMod.prefix((String) "progression_end"))) {
            event.getEntity().sendSystemMessage((Component) Component.translatable((String) "gui.twilightforest_finale.progression_end.message", (Object[]) new Object[]{Component.translatable((String) "gui.twilightforest.progression_end.discord").withStyle(style -> style.withColor(ChatFormatting.BLUE).applyFormat(ChatFormatting.UNDERLINE).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.experiment115.com/")))}));
        }
    }
}

