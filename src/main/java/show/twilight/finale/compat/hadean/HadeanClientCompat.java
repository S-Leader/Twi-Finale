package show.twilight.finale.compat.hadean;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import show.twilight.finale.client.renderer.DuckEnforcerTrophyRenderer;
import show.twilight.finale.client.renderer.HadeanBlasterTrophyRenderer;
import show.twilight.finale.client.renderer.HadeanEnforcerTrophyRenderer;
import show.twilight.finale.client.renderer.HadeanSmasherTrophyRenderer;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import twilightforest.compat.curios.renderer.CurioHeadRenderer;

public class HadeanClientCompat {
    public static void registerBlockEntityRenderers() {
        BlockEntityRenderers.register((BlockEntityType) ((BlockEntityType) ModHadeanBlockCompat.HADEAN_ENFORCER_TROPHY_ENTITY.get()), HadeanEnforcerTrophyRenderer::new);
        BlockEntityRenderers.register((BlockEntityType) ((BlockEntityType) ModHadeanBlockCompat.HADEAN_BLASTER_TROPHY_ENTITY.get()), HadeanBlasterTrophyRenderer::new);
        BlockEntityRenderers.register((BlockEntityType) ((BlockEntityType) ModHadeanBlockCompat.HADEAN_SMASHER_TROPHY_ENTITY.get()), HadeanSmasherTrophyRenderer::new);
        BlockEntityRenderers.register((BlockEntityType) ((BlockEntityType) ModHadeanBlockCompat.DUCK_ENFORCER_TROPHY_ENTITY.get()), DuckEnforcerTrophyRenderer::new);
    }

    public static void registerCuriosRenderers() {
        CuriosRendererRegistry.register((Item) ((Item) ModHadeanBlockCompat.HADEAN_ENFORCER_TROPHY.get()), CurioHeadRenderer::new);
        CuriosRendererRegistry.register((Item) ((Item) ModHadeanBlockCompat.HADEAN_BLASTER_TROPHY.get()), CurioHeadRenderer::new);
        CuriosRendererRegistry.register((Item) ((Item) ModHadeanBlockCompat.HADEAN_SMASHER_TROPHY.get()), CurioHeadRenderer::new);
        CuriosRendererRegistry.register((Item) ((Item) ModHadeanBlockCompat.DUCK_ENFORCER_TROPHY.get()), CurioHeadRenderer::new);
    }
}

