package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin extends Vec3i implements LuaUserData {
    public BlockPosMixin(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "X" -> this.getX();
            case "Y" -> this.getY();
            case "Z" -> this.getZ();
            default -> LuaUserData.super.vellum$getField(field);
        };
    }
}
