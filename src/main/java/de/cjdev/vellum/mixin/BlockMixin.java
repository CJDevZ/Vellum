package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
public abstract class BlockMixin implements LuaUserData {
    @Shadow
    public abstract BlockState defaultBlockState();

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "DefaultState" -> this.defaultBlockState();
            default -> LuaUserData.super.vellum$getField(field);
        };
    }
}
