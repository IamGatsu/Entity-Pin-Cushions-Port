package traben.entity_pin_cushions.mixin;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import traben.entity_pin_cushions.EntityPinCushions;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    /*
     * NOTE (1.21.11 port): as of the 1.21.9 rendering rewrite, EntityRenderDispatcher no
     * longer has a single render(Entity, ...) method that both reads the live Entity AND
     * draws it in one call. Rendering is now split into two steps:
     *   1) extractEntity(Entity, float) -> EntityRenderState   (has the live Entity)
     *   2) submit(EntityRenderState, CameraRenderState, ...)   (only has the render state)
     *
     * PinCushionLayer only ever sees step 2 (via RenderLayer#submit), so we hook step 1
     * here and stash the arrow/stinger counts in EntityPinCushions.PINCUSHION_DATA, keyed
     * by the exact EntityRenderState instance that extractEntity() returns. That's more
     * robust than the old "just overwrite some static ints" approach, since extraction and
     * submission are no longer guaranteed to interleave entity-by-entity.
     */
    @Inject(method = "extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN"))
    private <E extends Entity> void allStuckArrows$captureEntity(final E entity, final float partialTick, final CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity instanceof LivingEntity alive) {
            EntityRenderState state = cir.getReturnValue();
            if (state != null) {
                EntityPinCushions.PINCUSHION_DATA.put(state, new int[]{entity.getId(), alive.getArrowCount(), alive.getStingerCount()});
            }
        }
    }
}
