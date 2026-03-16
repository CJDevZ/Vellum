package de.cjdev.scrolls;

import net.hollowcube.luau.LuaState;

public record LuaObjRef(LuaState source, int reference) {
}
