package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import de.cjdev.scrolls.Scrolls;
import net.hollowcube.luau.LuaState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements LuaUserData {
    protected BlockStateMixin(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        super(owner, propertyKeys, propertyValues);
    }

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "Block" -> this.getBlock();
            default -> null;
        };
    }

    @Override
    public int scrolls$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "With" -> {
                Scrolls.pushLua(state, this.trySetValue((Property) args[0], (Comparable) args[1]));
                return 1;
            }
        }
        return 0;
    }
}
