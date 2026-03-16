package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3.class)
public class Vec3Mixin implements LuaUserData {
    @Shadow
    @Final
    public double x;

    @Shadow
    @Final
    public double y;

    @Shadow
    @Final
    public double z;

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "X" -> this.x;
            case "Y" -> this.y;
            case "Z" -> this.z;
            default -> null;
        };
    }
}
