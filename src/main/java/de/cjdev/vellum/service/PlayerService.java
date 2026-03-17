package de.cjdev.vellum.service;

import de.cjdev.vellum.LuaEvent;
import de.cjdev.vellum.LuaUserData;
import de.cjdev.vellum.Vellum;
import net.hollowcube.luau.LuaState;
import net.minecraft.server.MinecraftServer;

public class PlayerService implements LuaUserData {

    private static final String LUA_NAME = "PlayerService";
    private final MinecraftServer SERVER;

    public final LuaEvent Join;
    public final LuaEvent Leave;

    public PlayerService(MinecraftServer server) {
        SERVER = server;
        Join = new LuaEvent();
        Leave = new LuaEvent();
    }

    @Override
    public String vellum$luaName() {
        return LUA_NAME;
    }

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "OnJoin" -> Join;
            case "OnLeave" -> Leave;
            default -> LuaUserData.super.vellum$getField(field);
        };
    }

    @Override
    public int vellum$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "GetPlayers" -> {
                Vellum.pushLua(state, SERVER.getPlayerList().getPlayers());
                return 1;
            }
        }
        return 0;
    }
}
