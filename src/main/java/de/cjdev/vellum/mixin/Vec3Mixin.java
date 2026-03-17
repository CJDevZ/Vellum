package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
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
    public String vellum$luaName() {
        return "Vec3";
    }

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "X" -> this.x;
            case "Y" -> this.y;
            case "Z" -> this.z;
            default -> LuaUserData.super.vellum$getField(field);
        };
    }
}
