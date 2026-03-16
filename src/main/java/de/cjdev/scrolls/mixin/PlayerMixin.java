package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin implements LuaUserData {
    @Shadow
    public abstract FoodData getFoodData();

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "Hunger" -> this.getFoodData().getFoodLevel();
            case "Saturation" -> this.getFoodData().getSaturationLevel();
            default -> super.scrolls$getField(field);
        };
    }

    @Override
    public void scrolls$setField(String field, Object value) {
        switch (field) {
            case "Hunger" -> this.getFoodData().setFoodLevel((int) value);
            case "Saturation" -> this.getFoodData().setSaturation((float) value);
            default -> super.scrolls$setField(field, value);
        }
    }
}
