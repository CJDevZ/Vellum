package de.cjdev.vellum.event;

import de.cjdev.vellum.LuaUserData;
import de.cjdev.vellum.Vellum;
import de.cjdev.vellum.service.EnumService;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.hollowcube.luau.LuaFunc;
import net.hollowcube.luau.LuaState;

import java.util.Map;
import java.util.function.Function;

public final class SetupEvent {
    public static final Event<RegisterClass> REGISTER_CLASS = EventFactory.createArrayBacked(RegisterClass.class, callbacks -> builder -> {
        for (RegisterClass callback : callbacks) {
            callback.onRegistration(builder);
        }
    });

    public static final Event<RegisterEnum> REGISTER_ENUM = EventFactory.createArrayBacked(RegisterEnum.class, callbacks -> builder -> {
        for (RegisterEnum callback : callbacks) {
            callback.onRegistration(builder);
        }
    });

    @FunctionalInterface
    public interface RegisterClass {
        void onRegistration(ClassBuilder builder);
    }

    @FunctionalInterface
    public interface RegisterEnum {
        void onRegistration(EnumBuilder builder);
    }

    public static class ClassBuilder {
        private final LuaState state;

        public ClassBuilder(final LuaState state) {
            this.state = state;
        }

        public <T extends LuaUserData> void add(String name, Function<Object[], T> newInstance) {
            state.pushFunction(LuaFunc.wrap(state -> {
                newInstance.apply(Vellum.stackToArgs(state, 0)).vellum$pushLua(state);
                return 1;
            }, "newInstance"));
            this.state.setGlobal(name);
        }
    }

    public static class EnumBuilder {
        private final Map<String, EnumService.LuaEnum> map;

        public EnumBuilder() {
            this.map = new Object2ObjectOpenHashMap<>();
        }

        public <T extends Enum<?>> void add(String name, Class<T> enumClass) {
            Map<String, Enum<?>> enumMap = new Object2ObjectOpenHashMap<>();
            for (T enumConstant : enumClass.getEnumConstants()) {
                enumMap.put(enumConstant.name(), enumConstant);
            }
            this.add(name, enumMap::get);
        }

        public void add(String name, Function<String, ?> lookup) {
            this.map.put(name, new EnumService.LuaEnum(lookup));
        }

        public Map<String, EnumService.LuaEnum> build() {
            return this.map;
        }
    }
}
