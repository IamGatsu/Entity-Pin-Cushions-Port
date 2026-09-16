package traben.entity_pin_cushions.compat;

import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Optional, reflection-only integration with the "TwoDProjectiles" mod (mod id
 * {@code twod_projectiles}). Entity Pin Cushions has no compile- or runtime
 * dependency on that mod - if it isn't installed, every method below is a
 * cheap no-op and {@link PinCushionLayer.ArrowLayer} renders its usual vanilla
 * 3D arrow model.
 * <p>
 * When TwoDProjectiles *is* installed and its own "render arrows as 2D" option
 * is turned on, {@link #isTwoDArrowActive()} returns true and the pin-cushion
 * arrow layer (PinCushionLayer.ArrowLayer) swaps to drawing a flat
 * {@code minecraft:arrow} item - the same way TwoDProjectiles itself renders
 * arrows stuck in vanilla mobs - so stuck arrows look consistent everywhere.
 */
public final class TwoDProjectilesCompat {

    private static final Logger LOGGER = LoggerFactory.getLogger("entity_pin_cushions/twod_projectiles_compat");

    private static final boolean PRESENT;
    private static Field configField;          // static TwoDProjectiles.CONFIG
    private static Field renderTwoDArrowField; // boolean TwoDProjectilesConfig.renderTwoDArrow
    private static Field arrowScaleField;      // float   TwoDProjectilesConfig.arrowScale
    private static Field arrowOffsetField;     // float   TwoDProjectilesConfig.arrowOffset
    private static Method getArrowAngleMethod; // static float TwoDProjectiles.getArrowAngle(ItemStack)

    // set once if anything above ever throws at runtime, so we don't keep spamming the log
    private static boolean broken = false;

    static {
        boolean present;
        try {
            Class<?> mainClass = Class.forName("com.gaura.twod_projectiles.TwoDProjectiles");
            Class<?> configClass = Class.forName("com.gaura.twod_projectiles.config.TwoDProjectilesConfig");

            configField = mainClass.getField("CONFIG");
            renderTwoDArrowField = configClass.getField("renderTwoDArrow");
            arrowScaleField = configClass.getField("arrowScale");
            arrowOffsetField = configClass.getField("arrowOffset");
            getArrowAngleMethod = mainClass.getMethod("getArrowAngle", ItemStack.class);

            present = true;
            LOGGER.info("Detected TwoDProjectiles - pin cushion arrows will match its 2D arrow style when enabled");
        } catch (Throwable t) {
            present = false;
            LOGGER.debug("TwoDProjectiles not present, skipping arrow-model compat", t);
        }
        PRESENT = present;
    }

    private TwoDProjectilesCompat() {
    }

    /**
     * @return true only if TwoDProjectiles is installed AND its "render arrows
     * as 2D" config option is currently switched on.
     */
    public static boolean isTwoDArrowActive() {
        if (!PRESENT || broken) return false;
        try {
            Object config = configField.get(null);
            return renderTwoDArrowField.getBoolean(config);
        } catch (Throwable t) {
            markBroken(t);
            return false;
        }
    }

    /** TwoDProjectiles' configured arrow scale, or 1.0 if unavailable. */
    public static float getArrowScale() {
        if (!PRESENT || broken) return 1.0F;
        try {
            Object config = configField.get(null);
            return arrowScaleField.getFloat(config);
        } catch (Throwable t) {
            markBroken(t);
            return 1.0F;
        }
    }

    /** TwoDProjectiles' configured arrow offset, or 0.0 if unavailable. */
    public static float getArrowOffset() {
        if (!PRESENT || broken) return 0.0F;
        try {
            Object config = configField.get(null);
            return arrowOffsetField.getFloat(config);
        } catch (Throwable t) {
            markBroken(t);
            return 0.0F;
        }
    }

    /** TwoDProjectiles' configured flat-facing angle for the given stack, or 0.0 if unavailable. */
    public static float getArrowAngle(ItemStack stack) {
        if (!PRESENT || broken) return 0.0F;
        try {
            return (float) getArrowAngleMethod.invoke(null, stack);
        } catch (Throwable t) {
            markBroken(t);
            return 0.0F;
        }
    }

    private static void markBroken(Throwable t) {
        broken = true;
        LOGGER.warn("TwoDProjectiles compat broke while reflecting into its config, disabling for this session", t);
    }
}
