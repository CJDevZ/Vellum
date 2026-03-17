package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import de.cjdev.vellum.Vellum;
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
    public Object vellum$getField(String field) {
        if (field.equals("Block")) {
            return this.getBlock();
        }
        return LuaUserData.super.vellum$getField(field);
    }

    @Override
    public int vellum$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "With" -> {
                Vellum.pushLua(state, this.trySetValue((Property) this.getBlock().getStateDefinition().getProperty((String) args[0]), (Comparable) args[1]));
                return 1;
            }
        }
        return 0;
    }
}
