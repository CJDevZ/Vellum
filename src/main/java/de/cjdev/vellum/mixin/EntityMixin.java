package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.hollowcube.luau.LuaState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin implements LuaUserData {
    @Shadow
    public abstract int getId();

    @Shadow
    public abstract String getStringUUID();

    @Shadow
    public abstract String getScoreboardName();

    @Shadow
    public abstract Vec3 position();

    @Shadow
    public abstract BlockPos blockPosition();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract void teleportTo(double x, double y, double z);

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "Id" -> this.getId();
            case "UUID" -> this.getStringUUID();
            case "Name" -> this.getScoreboardName();
            case "Pos" -> this.position();
            case "BlockPos" -> this.blockPosition();
            case "Yaw" -> this.yRot;
            case "Pitch" -> this.xRot;
            case "World" -> this.level();
            default -> LuaUserData.super.vellum$getField(field);
        };
    }

    @Override
    public int vellum$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "Teleport" -> {
                var pos = (Vec3)args[0];
                if (pos != null) {
                    this.teleportTo(pos.x, pos.y, pos.z);
                }
            }
        }
        return 0;
    }
}
