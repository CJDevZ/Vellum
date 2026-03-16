package de.cjdev.scrolls.service;

import de.cjdev.scrolls.LuaUserData;
import net.hollowcube.luau.LuaState;

import java.util.Map;
import java.util.function.Function;

public class EnumService implements LuaUserData {
    private final Map<String, LuaEnum> enums;

    public EnumService(Map<String, LuaEnum> enums) {
        this.enums = enums;
    }

    @Override
    public Object scrolls$getField(String field) {
        return enums.get(field);
    }

    public static class LuaEnum implements LuaUserData {

        private final Function<String, ?> lookup;

        public LuaEnum(Function<String, ?> lookup) {
            this.lookup = lookup;
        }

        public Object byName(String name) {
            return this.lookup.apply(name);
        }

        @Override
        public void pushLua(LuaState state) {
            state.newUserData(this);
            state.getMetaTable("LuaEnumClass");
            state.setMetaTable(-2);
        }
    }
}
