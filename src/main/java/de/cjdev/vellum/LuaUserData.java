package de.cjdev.vellum;

import net.hollowcube.luau.LuaState;

import java.util.Objects;

public interface LuaUserData {
    String vellum$luaName();
    default void scrolls$pushLua(LuaState state) {
        state.newUserData(this);
        state.getMetaTable("LuaUserData");
        state.setMetaTable(-2);
    }
    default Object vellum$getField(String field) {
        if (Objects.equals(field, "ClassName")) {
            return this.vellum$luaName();
        }
        return null;
    }
    default void vellum$setField(String field, Object value) {
    }
    default int vellum$call(LuaState state, String method, Object... args) {
        return 0;
    }
}
