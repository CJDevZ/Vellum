package de.cjdev.scrolls;

import net.hollowcube.luau.LuaState;

import java.util.ArrayList;
import java.util.List;

public class LuaEvent implements LuaUserData {

    private final List<LuaCallback> handlers;

    public LuaEvent() {
        this.handlers = new ArrayList<>();
    }

    public void fire(Object... args) {
        for (LuaCallback handler : this.handlers) {
            handler.invoke(args);
        }
    }

    public void register(LuaCallback callback) {
        this.handlers.add(callback);
    }

    @Override
    public int scrolls$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "Fire" -> fire(args);
            case "Connect" -> register((LuaCallback) args[0]);
        }
        return 0;
    }
}
