package de.cjdev.vellum.service;

import de.cjdev.vellum.LuaUserData;
import net.hollowcube.luau.LuaState;

import java.util.Map;
import java.util.function.Function;

public class EnumService implements LuaUserData {
    private static final String LUA_NAME = "EnumService";

    private final Map<String, LuaEnum> enums;

    public EnumService(Map<String, LuaEnum> enums) {
        this.enums = enums;
    }

    @Override
    public String vellum$luaName() {
        return LUA_NAME;
    }

    @Override
    public Object vellum$getField(String field) {
        return enums.get(field);
    }

    public static class LuaEnum implements LuaUserData {
        private static final String LUA_NAME = "LuaEnum";
        private final Function<String, ?> lookup;

        public LuaEnum(Function<String, ?> lookup) {
            this.lookup = lookup;
        }

        public Object byName(String name) {
            return this.lookup.apply(name);
        }

        @Override
        public void vellum$pushLua(LuaState state) {
            state.newUserData(this);
            state.getMetaTable("LuaEnumClass");
            state.setMetaTable(-2);
        }

        @Override
        public String vellum$luaName() {
            return LUA_NAME;
        }
    }
}
