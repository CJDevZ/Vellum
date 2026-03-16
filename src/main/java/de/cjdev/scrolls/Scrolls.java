package de.cjdev.scrolls;

import com.mojang.logging.LogUtils;
import de.cjdev.scrolls.event.SetupEvent;
import de.cjdev.scrolls.service.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hollowcube.luau.LuaFunc;
import net.hollowcube.luau.LuaState;
import net.hollowcube.luau.LuaString;
import net.hollowcube.luau.compiler.LuauCompileException;
import net.hollowcube.luau.compiler.LuauCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Scrolls implements ModInitializer {

    private static LuaState GLOBAL = null;
    private static final Logger LOGGER = LogUtils.getLogger();

    public static PlayerService PLAYER_SERVICE = null;
    public static TickService TICK_SERVICE = null;
    public static WorldService WORLD_SERVICE = null;
    public static CommandService COMMAND_SERVICE = null;

    public static final Function<Identifier, Block> BLOCK_LOOKUP = BuiltInRegistries.BLOCK::getValue;

    private static void registerClasses(LuaState state) {
        //ModDataComponents.load();

        SetupEvent.ClassBuilder classBuilder = new SetupEvent.ClassBuilder(state);
        classBuilder.add("Vec3", args -> (LuaUserData) new Vec3(((Number)args[0]).doubleValue(), ((Number)args[1]).doubleValue(), ((Number)args[2]).doubleValue()));
        classBuilder.add("BlockPos", args -> (LuaUserData) new BlockPos(((Number)args[0]).intValue(), ((Number)args[1]).intValue(), ((Number)args[2]).intValue()));
        SetupEvent.REGISTER_CLASS.invoker().onRegistration(classBuilder);

        SetupEvent.EnumBuilder enumBuilder = new SetupEvent.EnumBuilder();
        enumBuilder.add("Direction", Direction.class);
        enumBuilder.add("Block", string -> BLOCK_LOOKUP.apply(Identifier.parse(string)));
        enumBuilder.add("GameMode", GameType.class);
        SetupEvent.REGISTER_ENUM.invoker().onRegistration(enumBuilder);
        EnumService enumService = new EnumService(enumBuilder.build());
        enumService.pushLua(state);
        state.setGlobal("Enum");
    }

    private static void initLuau(MinecraftServer server) {
        GLOBAL.openLibs();

        // print
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            var builder = new StringBuilder();
            stackToString(state, builder);
            LOGGER.info(builder.toString());
            return 0;
        }, "print"));
        GLOBAL.setGlobal("print");

        // LuaUserData start
        GLOBAL.newMetaTable("LuaUserData");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            String field = state.toString(2);
            pushLua(state, userData.scrolls$getField(field));
            return 1;
        }, "__index"));
        GLOBAL.setField(-2, "__index");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            String field = state.toString(2);
            userData.scrolls$setField(field, fromStack(state, 3));
            return 0;
        }, "__newindex"));
        GLOBAL.setField(-2, "__newindex");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            LuaString methodName = state.nameCallAtom();
            String method = ((LuaString.Str) methodName).str();
            return userData.scrolls$call(state, method, stackToArgs(state));
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

        // LuaEnumClass start
        GLOBAL.newMetaTable("LuaEnumClass");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            EnumService.LuaEnum userData = (EnumService.LuaEnum) state.checkUserData(1, "LuaEnumClass");
            String enumName = state.toString(2);
            var enumValue = userData.byName(enumName);
            pushLua(state, enumValue);
            return 1;
        }, "__index"));
        GLOBAL.setField(-2, "__index");
        GLOBAL.pop(1);
        // LuaEnumClass end

        // LuaEnum start
        GLOBAL.newMetaTable("LuaEnum");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            Enum<?> userData = (Enum<?>) state.checkUserData(1, "LuaEnum");
            state.pushString(userData.name());
            return 1;
        }, "__tostring"));
        GLOBAL.setField(-2, "__tostring");
        GLOBAL.pop(1);
        // LuaEnum end

        // PlayerEvents
        PLAYER_SERVICE.pushLua(GLOBAL);
        GLOBAL.setGlobal("PlayerService");
        TICK_SERVICE.pushLua(GLOBAL);
        GLOBAL.setGlobal("TickService");
        COMMAND_SERVICE.pushLua(GLOBAL);
        GLOBAL.setGlobal("CommandService");

        // Server
        ((LuaUserData) server).pushLua(GLOBAL);
        GLOBAL.setGlobal("Server");

        registerClasses(GLOBAL);

        GLOBAL.sandbox();
    }

    public static Object fromStack(LuaState state, int index) {
        return switch (state.type(index)) {
            case NUMBER -> state.toNumber(index);
            case BOOLEAN -> state.toBoolean(index);
            case STRING -> state.toString(index);
            case USERDATA -> state.toUserData(index);
            case NIL -> null;
            case FUNCTION -> new LuaCallback(state, state.ref(index));
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

            case LuaCallback func -> func.state().rawGetI(LuaState.REGISTRY_INDEX, func.funcRef());
            case LuaUserData userData -> userData.pushLua(state);
            case Enum enumConstant -> {
                state.newUserData(enumConstant);
                state.getMetaTable("LuaEnum");
                state.setMetaTable(-2);
            }

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
        int i = 0;
        while (i < range) {
            objects[i] = fromStack(state, ++i + 1);
        }
        return objects;
    }

    public static Object[] stackToStaticArgs(LuaState state) {
        int top = state.top();
        Object[] objects = new Object[top];
        int i = 0;
        while (i < top) {
            objects[i] = fromStack(state, ++i);
        }
        return objects;
    }

    public static void stackToString(LuaState state, StringBuilder builder) {
        int top = state.top();
        if (top == 0) return;
        for (int i = 1; i < top; i++) {
            builder.append(fromStack(state, i));
            builder.append(" ");
        }
        builder.append(fromStack(state, top));
    }

    public static void runLuau(String chunkName, String script) throws LuauCompileException {
        var thread = GLOBAL.newThread();
        thread.sandboxThread();

        final byte[] bytecode = LuauCompiler.DEFAULT.compile(script);
        thread.load(chunkName, bytecode);
        thread.call(0, 0);
    }

    public static void runScripts(MinecraftServer server) {
        Path worldFolder = server.getWorldPath(LevelResource.ROOT);
        Path scriptsFolder = worldFolder.resolve("luau");
        if (!Files.exists(scriptsFolder)) {
            return;
        }
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
    }

    private static void refreshServices(MinecraftServer server) {
        PLAYER_SERVICE = new PlayerService(server);
        TICK_SERVICE = new TickService();
        WORLD_SERVICE = new WorldService(server);
        COMMAND_SERVICE = new CommandService(server);
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            refreshServices(server);

            GLOBAL = LuaState.newState();
            initLuau(server);
            runScripts(server);
        });

        ServerPlayerEvents.JOIN.register(serverPlayer -> PLAYER_SERVICE.Join.fire(serverPlayer));
        ServerPlayerEvents.LEAVE.register(serverPlayer -> PLAYER_SERVICE.Leave.fire(serverPlayer));
        ServerTickEvents.START_SERVER_TICK.register(server -> TICK_SERVICE.ServerTickStart.fire());
        ServerTickEvents.START_SERVER_TICK.register(server -> TICK_SERVICE.ServerTickEnd.fire());

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
            refreshServices(server);
            if (GLOBAL != null) {
                GLOBAL.close();
            }
            GLOBAL = LuaState.newState();
            initLuau(server);
            runScripts(server);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (GLOBAL != null) {
                GLOBAL.close();
            }
        }));
    }
}
