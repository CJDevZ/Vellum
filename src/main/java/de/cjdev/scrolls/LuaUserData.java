package de.cjdev.scrolls;

import net.hollowcube.luau.LuaState;

public interface LuaUserData {
    default void pushLua(LuaState state) {
        state.newUserData(this);
        state.getMetaTable("LuaUserData");
        state.setMetaTable(-2);
    }
    default Object getLuaField(String field) {
        return null;
    }
    default void setLuaField(String field, Object value) {
    }
    default int callMethod(LuaState state, String method, Object... args) {
        return 0;
    }
}
