package de.cjdev.scrolls;

import com.mojang.logging.LogUtils;
import de.cjdev.scrolls.service.PlayerService;
import de.cjdev.scrolls.service.TickService;
import de.cjdev.scrolls.service.WorldService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hollowcube.luau.LuaFunc;
import net.hollowcube.luau.LuaState;
import net.hollowcube.luau.LuaString;
import net.hollowcube.luau.compiler.LuauCompileException;
import net.hollowcube.luau.compiler.LuauCompiler;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Scrolls implements ModInitializer {

    private static LuaState GLOBAL = null;
    private static final Logger LOGGER = LogUtils.getLogger();

    public static PlayerService PLAYER_SERVICE = null;
    public static TickService TICK_SERVICE = null;
    public static WorldService WORLD_SERVICE = null;

    private static void initLuau() {
        GLOBAL.openLibs();

        // print
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            var builder = new StringBuilder();
            stackToString(state, builder);
            LOGGER.info(builder.toString());
            return 0;
        }, "print"));
        GLOBAL.setGlobal("print");

        // PlayerEvents
        PLAYER_SERVICE.pushLua(GLOBAL);
        GLOBAL.setGlobal("PlayerService");
        TICK_SERVICE.pushLua(GLOBAL);
        GLOBAL.setGlobal("TickService");

        // LuaUserData start
        GLOBAL.newMetaTable("LuaUserData");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            String field = state.toString(2);
            pushLua(state, userData.getLuaField(field));
            return 1;
        }, "__index"));
        GLOBAL.setField(-2, "__index");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            String field = state.toString(2);
            userData.setLuaField(field, fromStack(state, 3));
            return 0;
        }, "__newindex"));
        GLOBAL.setField(-2, "__newindex");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            LuaString methodName = state.nameCallAtom();
            String method = ((LuaString.Str) methodName).str();
            return userData.callMethod(state, method, stackToArgs(state));
        }, "__namecall"));
        GLOBAL.setField(-2, "__namecall");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            state.pushString(userData.toString());
            return 1;
        }, "__tostring"));
        GLOBAL.setField(-2, "__tostring");
        GLOBAL.pop(1);
        // LuaUserData end

        GLOBAL.sandbox();
    }

    public static Object fromStack(LuaState state, int index) {
        return switch (state.type(index)) {
            case NUMBER -> state.toNumber(index);
            case BOOLEAN -> state.toBoolean(index);
            case STRING -> state.toString(index);
            case USERDATA -> state.toUserData(index);
            case NIL -> null;
            default -> state.ref(index);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void pushLua(LuaState state, @Nullable Object obj) {
        if (obj == null) {
            state.pushNil();
            return;
        }
        if (obj.getClass().isArray()) {
            pushArray(state, obj);
            return;
        }
        switch (obj) {
            case String string -> state.pushString(string);
            case Integer number -> state.pushInteger(number);
            case Number number -> state.pushNumber(number.doubleValue());
            case List list -> pushList(state, list);

            case LuaFunc func -> state.pushFunction(func);
            case LuaUserData userData -> userData.pushLua(state);

            default -> state.pushNil();
        }
    }

    public static void pushList(LuaState state, List<Object> list) {
        var size = list.size();
        state.createTable(size, 0);
        int i = 0;
        while (i < size) {
            pushLua(state, list.get(i));
            state.rawSetI(-2, ++i);
        }
    }

    public static void pushArray(LuaState state, Object... array) {
        int length = array.length;
        state.createTable(length, 0);
        int i = 0;
        while (i < length) {
            pushLua(state, array[i]);
            state.rawSetI(-2, ++i);
        }
    }

    public static void pushUserData(LuaState state, LuaUserData object, String typeName) {
        state.newUserData(object);
        state.getMetaTable(typeName);
        state.setMetaTable(-2);
    }

    public static void pushTable(LuaState state, Map<String, Object> map) {
        state.createTable(0, map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            pushLua(state, entry.getValue());
            state.setField(-2, entry.getKey());
        }
    }

    public static Object[] stackToArgs(LuaState state) {
        int top = state.top();
        int range = top - 1;
        Object[] objects = new Object[range];
        for (int i = 0; i < range; i++) {
            objects[i] = fromStack(state, i + 2);
        }
        return objects;
    }

    public static void stackToString(LuaState state, StringBuilder builder) {
        int top = state.top();
        if (top == 0) return;
        state.getGlobal("tostring");
        for (int i = 1; i < top; i++) {
            state.pushValue(-1);
            state.pushValue(i);
            state.call(1, 1);

            builder.append(state.toString(-1));
            state.pop(1);
            builder.append(" ");
        }
        state.pushValue(-1);
        state.pushValue(top);
        state.call(1, 1);

        builder.append(state.toString(-1));
        state.pop(2);
    }

    public static void runLuau(String chunkName, String script) throws LuauCompileException {
        var thread = GLOBAL.newThread();
        thread.sandboxThread();

        final byte[] bytecode = LuauCompiler.DEFAULT.compile(script);
        thread.load(chunkName, bytecode);
        thread.call(0, 0);
    }

    @Override
    public void onInitialize() {
        GLOBAL = LuaState.newState();
        initLuau();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PLAYER_SERVICE = new PlayerService(server);
            TICK_SERVICE = new TickService();
            WORLD_SERVICE = new WorldService(server);

            Path worldFolder = server.getWorldPath(LevelResource.ROOT);
            LOGGER.info("World folder: {}", worldFolder);
            Path scriptsFolder = worldFolder.resolve("luau");
            try (var paths = Files.list(scriptsFolder)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".luau"))
                        .forEach(path -> {
                            try {
                                runLuau(path.toString(), Files.readString(path, StandardCharsets.UTF_8));
                            } catch (LuauCompileException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        ServerPlayerEvents.JOIN.register(PLAYER_SERVICE.Join::fire);
        ServerPlayerEvents.LEAVE.register(PLAYER_SERVICE.Leave::fire);
        ServerTickEvents.START_SERVER_TICK.register(TICK_SERVICE.ServerTickStart::fire);
        ServerTickEvents.START_SERVER_TICK.register(TICK_SERVICE.ServerTickEnd::fire);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (GLOBAL != null) {
                GLOBAL.close();
            }
        }));
    }
}
