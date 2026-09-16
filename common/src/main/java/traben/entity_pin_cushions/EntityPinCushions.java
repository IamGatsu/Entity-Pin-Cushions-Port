package traben.entity_pin_cushions;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.Map;
import java.util.WeakHashMap;

public final class EntityPinCushions {
    public static final String MOD_ID = "entity_pin_cushions";

    /**
     * Since the 1.21.9 rendering rewrite, entity rendering is split into an "extract"
     * step (still has the live Entity) and a later "submit" step (only gets the
     * already-extracted EntityRenderState, no Entity reference anymore). We capture
     * the arrow/stinger counts during extraction (see MixinEntityRenderDispatcher)
     * and stash them here keyed by the render-state object itself, since that's the
     * only thing PinCushionLayer still has access to during submission.
     * <p>
     * int[]{ entityId, arrowCount, stingerCount }
     */
    public static final Map<EntityRenderState, int[]> PINCUSHION_DATA = new WeakHashMap<>();

    public static void init() {
        // Write common init code here.

    }

}
