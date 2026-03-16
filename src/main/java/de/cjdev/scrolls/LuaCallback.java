package de.cjdev.scrolls;

import net.hollowcube.luau.LuaState;

public record LuaCallback(LuaState state, int funcRef) {
    public void invoke(Object... args) {
        this.state.rawGetI(LuaState.REGISTRY_INDEX, this.funcRef);
        for (Object arg : args) {
            Scrolls.pushLua(this.state, arg);
        }
        this.state.call(args.length, 0);
    }
}
