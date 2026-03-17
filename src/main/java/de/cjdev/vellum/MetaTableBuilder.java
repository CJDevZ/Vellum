package de.cjdev.vellum;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.hollowcube.luau.LuaFunc;
import net.hollowcube.luau.LuaState;

import java.util.Map;
import java.util.function.ToIntFunction;

public class MetaTableBuilder {
    private final Map<String, ToIntFunction<LuaState>> functionMap;

    protected MetaTableBuilder(Map<String, ToIntFunction<LuaState>> functionMap) {
        this.functionMap = functionMap;
    }

    public static MetaTableBuilder builder() {
        return new MetaTableBuilder(new Object2ObjectOpenHashMap<>());
    }

    public MetaTableBuilder index(ToIntFunction<LuaState> function) {
        return this.method("__index", function);
    }

    public MetaTableBuilder newIndex(ToIntFunction<LuaState> function) {
        return this.method("__newindex", function);
    }

    public MetaTableBuilder nameCall(ToIntFunction<LuaState> function) {
        return this.method("__namecall", function);
    }

    public MetaTableBuilder toString(ToIntFunction<LuaState> function) {
        return this.method("__tostring", function);
    }

    public MetaTableBuilder method(String methodName, ToIntFunction<LuaState> function) {
        this.functionMap.put(methodName, function);
        return this;
    }

    public void build(LuaState state, String metaTableName) {
        state.newMetaTable(metaTableName);
        for (Map.Entry<String, ToIntFunction<LuaState>> entry : functionMap.entrySet()) {
            String methodName = entry.getKey();
            String debugName = metaTableName + "." + methodName;
            state.pushFunction(LuaFunc.wrap(entry.getValue(), debugName));
            state.setField(-2, methodName);
        }
        state.pop(1);
    }
}
