package de.cjdev.vellum.service;

import de.cjdev.vellum.LuaEvent;
import de.cjdev.vellum.LuaUserData;

public class TickService implements LuaUserData {

    public static final String LUA_NAME = "TickService";

    public final LuaEvent ServerTickStart = new LuaEvent();
    public final LuaEvent ServerTickEnd = new LuaEvent();

    @Override
    public String vellum$luaName() {
        return LUA_NAME;
    }

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "ServerTickStart" -> ServerTickStart;
            case "ServerTickEnd" -> ServerTickEnd;
            default -> LuaUserData.super.vellum$getField(field);
        };
    }
}
