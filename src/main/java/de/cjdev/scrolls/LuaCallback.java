package de.cjdev.scrolls;

import net.hollowcube.luau.LuaState;
import net.hollowcube.luau.LuaType;

public record LuaCallback(LuaState source, int reference) {
    public void invoke(Object... args) {
        if (source.rawGetI(LuaState.REGISTRY_INDEX, this.reference) == LuaType.FUNCTION) {
            for (Object arg : args) {
                Scrolls.pushLua(source, arg);
            }
            source.call(args.length, 0);
        }
    }
}
