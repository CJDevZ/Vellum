package de.cjdev.scrolls.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cjdev.scrolls.LuaUserData;
import net.hollowcube.luau.LuaState;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
public abstract class WorldMixin extends Level implements LuaUserData {
    protected WorldMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Override
    public int callMethod(LuaState state, String method, Object... args) {
        switch (method) {
            case "SetBlock" -> {
                try {
                    int flags = args.length > 4 ? (int) args[4] : Block.UPDATE_CLIENTS;
                    var result = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, (String) args[3], true);
                    this.setBlock(new BlockPos((int)args[0],(int)args[1],(int)args[2]), result.blockState(), flags);
                } catch (CommandSyntaxException _) {
                }
            }
        }
        return 0;
    }
}
