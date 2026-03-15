package de.cjdev.scrolls.service;

import de.cjdev.scrolls.LuaEvent;
import de.cjdev.scrolls.LuaUserData;
import de.cjdev.scrolls.Scrolls;
import net.hollowcube.luau.LuaState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;

public class WorldService implements LuaUserData {

    private final MinecraftServer SERVER;

    private final LuaEvent BlockPlaced = new LuaEvent();
    private final LuaEvent BlockBroken = new LuaEvent();

    public WorldService(MinecraftServer server) {
        SERVER = server;
    }

    @Override
    public int callMethod(LuaState state, String method, Object... args) {
        switch (method) {
            case "Get" -> {
                Scrolls.pushLua(state, SERVER.levels.get(ResourceKey.create(Registries.DIMENSION, Identifier.parse((String) args[0]))));
                return 1;
            }
        }
        return 0;
    }

    @Override
    public Object getLuaField(String field) {
        return switch (field) {
            case "BlockPlaced" -> BlockPlaced;
            case "BlockBroken" -> BlockBroken;
            default -> null;
        };
    }
}
