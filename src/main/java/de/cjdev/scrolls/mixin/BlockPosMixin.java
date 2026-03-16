package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin extends Vec3i implements LuaUserData {
    public BlockPosMixin(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "X" -> this.getX();
            case "Y" -> this.getY();
            case "Z" -> this.getZ();
            default -> null;
        };
    }
}
