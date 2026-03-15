package de.cjdev.scrolls.mixin;

import com.mojang.authlib.GameProfile;
import de.cjdev.scrolls.LuaUserData;
import net.hollowcube.luau.LuaState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class PlayerMixin extends Player implements LuaUserData {
    @Shadow
    @Final
    public ServerPlayerGameMode gameMode;

    @Shadow
    public abstract boolean setGameMode(GameType mode);

    @Shadow
    public abstract ServerLevel level();

    public PlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public Object getLuaField(String field) {
        switch (field) {
            case "Id" -> this.getId();
            case "Name" -> this.getScoreboardName();
            case "UUID" -> this.getStringUUID();
            case "Health" -> this.getHealth();
            case "Hunger" -> this.getFoodData().getFoodLevel();
            case "Saturation" -> this.getFoodData().getSaturationLevel();
            case "GameMode" -> this.gameMode.getGameModeForPlayer().getName();
            case "World" -> this.level();
            default -> {
                return 0;
            }
        }
        return 1;
    }

    @Override
    public void setLuaField(String field, Object value) {
        switch (field) {
            case "Health" -> this.setHealth((float)value);
            case "Hunger" -> this.getFoodData().setFoodLevel((int) value);
            case "Saturation" -> this.getFoodData().setSaturation((float) value);
            case "GameMode" -> this.setGameMode(GameType.byName((String) value));
        }
    }

    @Override
    public int callMethod(LuaState state, String method, Object... args) {
        switch (method) {
            case "Teleport" -> {
                double x = (double)args[0], y = (double)args[1], z = (double)args[2];
                this.teleportTo(x, y, z);
            }
            case "GetBlockPos" -> {
                var pos = this.blockPosition();
                state.pushInteger(pos.getX());
                state.pushInteger(pos.getY());
                state.pushInteger(pos.getZ());
                return 3;
            }
            case "GetPos" -> {
                var pos = this.position();
                state.pushNumber(pos.x);
                state.pushNumber(pos.y);
                state.pushNumber(pos.z);
                return 3;
            }
        }
        return 0;
    }
}
