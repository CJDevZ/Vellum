package de.cjdev.scrolls;

import net.hollowcube.luau.LuaState;

public interface LuaUserData {
    default void pushLua(LuaState state) {
        state.newUserData(this);
        state.getMetaTable("LuaUserData");
        state.setMetaTable(-2);
    }
    default Object scrolls$getField(String field) {
        return null;
    }
    default void scrolls$setField(String field, Object value) {
    }
    default int scrolls$call(LuaState state, String method, Object... args) {
        return 0;
    }
}
