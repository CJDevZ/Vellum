package de.cjdev.vellum.service;

import de.cjdev.vellum.LuaEvent;
import de.cjdev.vellum.LuaUserData;
import de.cjdev.vellum.Vellum;
import net.hollowcube.luau.LuaState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandService implements LuaUserData {

    private static final String LUA_NAME = "CommandService";
    private final MinecraftServer SERVER;

    private final LuaEvent BlockPlaced = new LuaEvent();

    public CommandService(MinecraftServer server) {
        SERVER = server;
    }

    @Override
    public int vellum$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "Register" -> {
                String commandName = (String) args[0];
                Vellum.pushLua(state, SERVER.levels.get(ResourceKey.create(Registries.DIMENSION, Identifier.parse((String) args[0]))));
                return 1;
            }
            case "Dispatch" -> {
                String command = (String) args[0];
                SERVER.getCommands().performPrefixedCommand(new CommandSourceStack(
                        SERVER,
                        Vec3.atLowerCornerOf(SERVER.getRespawnData().pos()),
                        Vec2.ZERO,
                        SERVER.findRespawnDimension(),
                        SERVER.getFunctionCompilationPermissions(),
                        "Luau",
                        Component.literal("Luau"),
                        SERVER,
                        null), command);
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
            default -> LuaUserData.super.vellum$getField(field);
        };
    }
}
