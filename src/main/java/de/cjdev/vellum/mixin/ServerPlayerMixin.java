package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements LuaUserData {
    @Shadow
    @Final
    public ServerPlayerGameMode gameMode;

    @Shadow
    public abstract boolean setGameMode(GameType mode);

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "GameMode" -> this.gameMode.getGameModeForPlayer();
            default -> super.vellum$getField(field);
        };
    }

    @Override
    public void vellum$setField(String field, Object value) {
        switch (field) {
            case "GameMode" -> this.setGameMode((GameType) value);
            default -> super.vellum$setField(field, value);
        }
    }

    @Override
    public String vellum$luaName() {
        return "ServerPlayer";
    }
}
