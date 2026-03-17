package de.cjdev.vellum.data;

import de.cjdev.vellum.LuaUserData;
import de.cjdev.vellum.Vellum;
import net.hollowcube.luau.LuaState;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityValue implements LuaUserData {
    public static final String LUA_NAME = "EntityValue";

    private final Map<UUID, Object> VALUES = new ConcurrentHashMap<>();

    public boolean hasKey(Entity entity) {
        return VALUES.containsKey(entity.getUUID());
    }

    public Object getValue(Entity entity) {
        return VALUES.get(entity.getUUID());
    }

    public void setValue(Entity entity, Object value) {
        VALUES.put(entity.getUUID(), value);
    }

    public void remove(Entity entity) {
        VALUES.remove(entity.getUUID());
    }

    @Override
    public String vellum$luaName() {
        return LUA_NAME;
    }

    @Override
    public int vellum$call(LuaState state, String method, Object... args) {
        switch (method) {
            case "Has" -> {
                state.pushBoolean(this.hasKey((Entity) args[0]));
                return 1;
            }
            case "Get" -> {
                Vellum.pushLua(state, this.getValue((Entity) args[0]));
                return 1;
            }
            case "Set" -> this.setValue((Entity) args[0], args[1]);
            case "Remove" -> this.remove((Entity) args[0]);
        }
        return 0;
    }
}
