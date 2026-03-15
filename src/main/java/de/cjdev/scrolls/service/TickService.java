package de.cjdev.scrolls.service;

import de.cjdev.scrolls.LuaEvent;
import de.cjdev.scrolls.LuaUserData;

public class TickService implements LuaUserData {

    public final LuaEvent ServerTickStart = new LuaEvent();
    public final LuaEvent ServerTickEnd = new LuaEvent();

    @Override
    public Object getLuaField(String field) {
        return switch (field) {
            case "ServerTickStart" -> ServerTickStart;
            case "ServerTickEnd" -> ServerTickEnd;
            default -> null;
        };
    }
}
