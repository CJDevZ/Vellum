package de.cjdev.vellum;

import net.hollowcube.luau.LuaState;

public record LuaObjRef(LuaState source, int reference) {
}
