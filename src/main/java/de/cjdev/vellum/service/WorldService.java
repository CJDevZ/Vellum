package de.cjdev.vellum.service;

import de.cjdev.vellum.LuaEvent;
import de.cjdev.vellum.LuaUserData;
import de.cjdev.vellum.Vellum;
import net.hollowcube.luau.LuaState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;

public class WorldService implements LuaUserData {

    public static final String LUA_NAME = "WorldService";
    private final MinecraftServer SERVER;

    private final LuaEvent BlockPlaced = new LuaEvent();
    private final LuaEvent BlockBroken = new LuaEvent();

    public WorldService(MinecraftServer server) {
        SERVER = server;
    }

    @Override
    public int vellum$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "Get" -> {
                Vellum.pushLua(state, SERVER.levels.get(ResourceKey.create(Registries.DIMENSION, Identifier.parse((String) args[0]))));
                return 1;
            }
        }
        return 0;
    }

    @Override
    public String vellum$luaName() {
        return LUA_NAME;
    }

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "BlockPlaced" -> BlockPlaced;
            case "BlockBroken" -> BlockBroken;
            default -> LuaUserData.super.vellum$getField(field);
        };
    }
}
