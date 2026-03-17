package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin implements LuaUserData {
    @Shadow
    public abstract FoodData getFoodData();

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "Hunger" -> this.getFoodData().getFoodLevel();
            case "Saturation" -> this.getFoodData().getSaturationLevel();
            default -> super.vellum$getField(field);
        };
    }

    @Override
    public void vellum$setField(String field, Object value) {
        switch (field) {
            case "Hunger" -> this.getFoodData().setFoodLevel((int) value);
            case "Saturation" -> this.getFoodData().setSaturation((float) value);
            default -> super.vellum$setField(field, value);
        }
    }
}
