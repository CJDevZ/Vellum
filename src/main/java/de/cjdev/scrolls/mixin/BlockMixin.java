package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
public abstract class BlockMixin implements LuaUserData {
    @Shadow
    public abstract BlockState defaultBlockState();

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "DefaultState" -> this.defaultBlockState();
            default -> null;
        };
    }
}
