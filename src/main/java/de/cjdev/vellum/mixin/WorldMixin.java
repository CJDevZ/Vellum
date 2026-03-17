package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import de.cjdev.vellum.Vellum;
import net.hollowcube.luau.LuaState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
public abstract class WorldMixin extends Level implements LuaUserData {
    protected WorldMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Override
    public int vellum$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "GetState" -> {
                Vellum.pushLua(state, this.getBlockState((BlockPos) args[0]));
                return 1;
            }
            case "SetState" -> {
                int flags = args.length > 2 ? (int) args[2] : Block.UPDATE_CLIENTS;
                this.setBlock((BlockPos) args[0], (BlockState) args[1], flags);
            }
        }
        return 0;
    }
}
