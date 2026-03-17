package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements LuaUserData {
    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float health);

    @Shadow
    public abstract boolean isFallFlying();

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "Health" -> this.getHealth();
            default -> super.vellum$getField(field);
        };
    }

    @Override
    public void vellum$setField(String field, Object value) {
        switch (field) {
            case "Health" -> this.setHealth((float)value);
            default -> super.vellum$setField(field, value);
        }
    }
}
