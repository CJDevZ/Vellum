package de.cjdev.scrolls;

import net.hollowcube.luau.LuaState;

import java.util.ArrayList;
import java.util.List;

public class LuaEvent implements LuaUserData {

    private final List<Handler> handlers;

    public LuaEvent() {
        this.handlers = new ArrayList<>();
    }

    public void fire(Object... args) {
        for (Handler handler : this.handlers) {
            LuaState state = handler.state;
            state.rawGetI(LuaState.REGISTRY_INDEX, handler.funcRef);
            for (Object arg : args) {
                Scrolls.pushLua(state, arg);
            }
            state.call(args.length, 0);
        }
    }

    public void register(LuaState state, int funcRef) {
        this.handlers.add(new Handler(state, funcRef));
    }

    private record Handler(LuaState state, int funcRef) {
    }

    @Override
    public int callMethod(LuaState state, String method, Object... args) {
        switch (method) {
            case "fire" -> fire(args);
            case "register" -> register(state, (int) args[0]);
        }
        return 0;
    }
}
