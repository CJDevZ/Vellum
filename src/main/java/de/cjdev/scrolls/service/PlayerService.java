package de.cjdev.scrolls.service;

import de.cjdev.scrolls.LuaEvent;
import de.cjdev.scrolls.LuaUserData;
import de.cjdev.scrolls.Scrolls;
import net.hollowcube.luau.LuaState;
import net.minecraft.server.MinecraftServer;

public class PlayerService implements LuaUserData {

    private final MinecraftServer SERVER;

    public final LuaEvent Join;
    public final LuaEvent Leave;

    public PlayerService(MinecraftServer server) {
        SERVER = server;
        Join = new LuaEvent();
        Leave = new LuaEvent();
    }

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "OnJoin" -> Join;
            case "OnLeave" -> Leave;
            default -> null;
        };
    }

    @Override
    public int scrolls$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "GetPlayers" -> {
                Scrolls.pushLua(state, SERVER.getPlayerList().getPlayers());
                return 1;
            }
        }
        return 0;
    }
}
