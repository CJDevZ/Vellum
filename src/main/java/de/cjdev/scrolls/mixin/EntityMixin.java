package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import de.cjdev.scrolls.ModDataComponents;
import net.hollowcube.luau.LuaState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
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
    public abstract <T> T get(DataComponentType<? extends T> type);

    @Shadow
    public abstract <T> void setComponent(DataComponentType<T> type, T value);

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "Id" -> this.getId();
            case "UUID" -> this.getStringUUID();
            case "Name" -> this.getScoreboardName();
            case "Pos" -> this.position();
            case "BlockPos" -> this.blockPosition();
            case "World" -> this.level();
            case "SavedData" -> {
                var data = this.get(ModDataComponents.PERSISTENT_DATA);
                if (data == null) {
                    data = new CompoundTag();
                    this.setComponent(ModDataComponents.PERSISTENT_DATA, data);
                }
                yield data;
            }
            default -> null;
        };
    }

    @Override
    public int scrolls$call(LuaState state, String method, Object... args) {
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
