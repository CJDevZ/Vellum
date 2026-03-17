package de.cjdev.vellum;

import com.mojang.logging.LogUtils;
import de.cjdev.vellum.data.EntityValue;
import de.cjdev.vellum.event.SetupEvent;
import de.cjdev.vellum.service.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.hollowcube.luau.LuaError;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Vellum implements ModInitializer {

    private static LuaState GLOBAL = null;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Path scriptsFolder;
    private static final Map<String, Object> moduleCache = new HashMap<>();
    static List<LuaState> THREADS = new ArrayList<>();

    public static PlayerService PLAYER_SERVICE = null;
    public static TickService TICK_SERVICE = null;
    public static WorldService WORLD_SERVICE = null;
    public static CommandService COMMAND_SERVICE = null;

    public static final Function<Identifier, Block> BLOCK_LOOKUP = BuiltInRegistries.BLOCK::getValue;

    private static void registerClasses(LuaState state) {
        SetupEvent.ClassBuilder classBuilder = new SetupEvent.ClassBuilder(state);
        classBuilder.add("Vec3", args -> (LuaUserData) new Vec3(((Number)args[0]).doubleValue(), ((Number)args[1]).doubleValue(), ((Number)args[2]).doubleValue()));
        classBuilder.add("BlockPos", args -> (LuaUserData) new BlockPos(((Number)args[0]).intValue(), ((Number)args[1]).intValue(), ((Number)args[2]).intValue()));
        classBuilder.add(EntityValue.LUA_NAME, _ -> new EntityValue());
        SetupEvent.REGISTER_CLASS.invoker().onRegistration(classBuilder);
    }

    private static void registerEnums(LuaState state) {
        SetupEvent.EnumBuilder enumBuilder = new SetupEvent.EnumBuilder();
        enumBuilder.add("Direction", Direction.class);
        enumBuilder.add("Block", string -> BLOCK_LOOKUP.apply(Identifier.parse(string)));
        enumBuilder.add("GameMode", GameType.class);
        SetupEvent.REGISTER_ENUM.invoker().onRegistration(enumBuilder);
        EnumService enumService = new EnumService(enumBuilder.build());
        enumService.scrolls$pushLua(state);
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

        // require
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            String scriptName = (String) fromStack(state, 1);
            Path scriptPath = scriptsFolder.resolve(scriptName + ".luau");
            try {
                LuaState moduleThread = runLuau(scriptName, Files.readString(scriptPath, StandardCharsets.UTF_8));
                Object moduleObject = fromStack(moduleThread, -1);
                pushLua(state, moduleObject);
            } catch (LuauCompileException | IOException e) {
                LOGGER.error("Error loading module {}", scriptName, e);
            }
            return 1;
        }, "require"));
        GLOBAL.setGlobal("require");

        // LuaUserData start
        GLOBAL.newMetaTable("LuaUserData");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            String field = state.toString(2);
            pushLua(state, userData.vellum$getField(field));
            return 1;
        }, "__index"));
        GLOBAL.setField(-2, "__index");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            String field = state.toString(2);
            userData.vellum$setField(field, fromStack(state, 3));
            return 0;
        }, "__newindex"));
        GLOBAL.setField(-2, "__newindex");
        GLOBAL.pushFunction(LuaFunc.wrap(state -> {
            LuaUserData userData = (LuaUserData) state.checkUserData(1, "LuaUserData");
            LuaString methodName = state.nameCallAtom();
            String method = ((LuaString.Str) methodName).str();
            return userData.vellum$call(state, method, stackToArgs(state, 1));
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
        PLAYER_SERVICE.scrolls$pushLua(GLOBAL);
        GLOBAL.setGlobal("PlayerService");
        TICK_SERVICE.scrolls$pushLua(GLOBAL);
        GLOBAL.setGlobal("TickService");
        COMMAND_SERVICE.scrolls$pushLua(GLOBAL);
        GLOBAL.setGlobal("CommandService");

        // Server
        ((LuaUserData) server).scrolls$pushLua(GLOBAL);
        GLOBAL.setGlobal("Server");

        registerClasses(GLOBAL);
        registerEnums(GLOBAL);

        GLOBAL.sandbox();
    }

    public static Object fromStack(LuaState state, int index) {
        return switch (state.type(index)) {
            case NIL -> null;
            case BOOLEAN -> state.toBoolean(index);
            case NUMBER -> state.toNumber(index);
            case VECTOR -> state.toVector(index);
            case STRING -> state.toString(index);
            case FUNCTION -> new LuaCallback(state, state.ref(index));
            case USERDATA -> state.toUserData(index);
            default -> new LuaObjRef(state, state.ref(index));
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void pushLua(LuaState state, @Nullable Object obj) {
        if (obj == null) {
            state.pushNil();
            return;
        }
        if (obj instanceof float[] vector) {
            state.pushVector(vector);
            return;
        }
        if (obj.getClass().isArray()) {
            pushArray(state, obj);
            return;
        }
        switch (obj) {
            case Boolean bool -> state.pushBoolean(bool);
            case String string -> state.pushString(string);
            case Integer number -> state.pushInteger(number);
            case Number number -> state.pushNumber(number.doubleValue());
            case List list -> pushList(state, list);

            case LuaObjRef ref -> {
                ref.source().rawGetI(LuaState.REGISTRY_INDEX, ref.reference());
                ref.source().xmove(state, 1);
            }
            case LuaCallback func -> {
                func.source().rawGetI(LuaState.REGISTRY_INDEX, func.reference());
                func.source().xmove(state, 1);
            }
            case LuaUserData userData -> userData.scrolls$pushLua(state);
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
        for (int i = 0; i < size; i++){
            pushLua(state, list.get(i));
            state.rawSetI(-2, i + 1);
        }
    }

    public static void pushArray(LuaState state, Object... array) {
        int length = array.length;
        state.createTable(length, 0);
        for (int i = 0; i < length; i++){
            pushLua(state, array[i]);
            state.rawSetI(-2, i + 1);
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

    public static Object[] stackToArgs(LuaState state, int stackOffset) {
        int top = state.top();
        int range = top - stackOffset;

        if (range <= 0) return new Object[0];

        Object[] objects = new Object[range];
        for (int i = 0, stackIndex = stackOffset + 1; i < range; i++, stackIndex++) {
            objects[i] = fromStack(state, stackIndex);
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

    public static LuaState runLuau(String chunkName, String script) throws LuauCompileException {
        final byte[] bytecode = LuauCompiler.DEFAULT.compile(script);

        var thread = sandboxedThread();
        THREADS.add(thread);

        thread.load(chunkName, bytecode);
        try {
            thread.call(0, 0);
        } catch (LuaError e) {
            LOGGER.error("Error running {}", chunkName, e);
        }
        return thread;
    }

    public static void runScripts(MinecraftServer server) {
        Path worldFolder = server.getWorldPath(LevelResource.ROOT);
        scriptsFolder = worldFolder.resolve("luau");
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
                            LOGGER.error("Error running {}", path, e);
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

    public static LuaState sandboxedThread() {
        var thread = GLOBAL.newThread();
        thread.sandboxThread();
        return thread;
    }

    @Override
    public void onInitialize() {
        // Todo: Add DataStores for Players
        // Todo: Add Storable Values which only live as long as the Object exists
        // Todo: Work on the most important aspects, and expand functionality
        // Todo: Stop crashes and instead log errors
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
            for (LuaState thread : THREADS) {
                thread.close();
            }
            THREADS.clear();

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
