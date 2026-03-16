package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import net.minecraft.nbt.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin implements LuaUserData {
    @Shadow
    public abstract @Nullable Tag get(String name);

    @Shadow
    public abstract @Nullable Tag put(String name, Tag tag);

    @Shadow
    public abstract @Nullable Tag remove(String name);

    @Override
    public Object scrolls$getField(String field) {
        var value = this.get(field);
        if (value == null) {
            return null;
        }
        return switch (value) {
            case IntTag intTag -> intTag.intValue();
            case NumericTag numericTag -> numericTag.asDouble();
            case StringTag stringTag -> stringTag.toString();
            default -> value;
        };
    }

    @Override
    public void scrolls$setField(String field, Object value) {
        if (value == null) {
            this.remove(field);
            return;
        }
        this.put(field, scrolls$getTag(value));
    }

    private static CollectionTag scrolls$getTagArray(Object[] array) {
        List<Tag> list = new ArrayList<>(array.length);
        for (Object o : array) {
            list.add(scrolls$getTag(o));
        }
        return new ListTag(list);
    }

    private static @NonNull Tag scrolls$getTag(@NonNull Object value) {
        if (value.getClass().isArray()) {
            return scrolls$getTagArray((Object[]) value);
        }
        return switch (value) {
            case Integer number -> IntTag.valueOf(number);
            case Double number -> DoubleTag.valueOf(number);
            case String string -> StringTag.valueOf(string);
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
    }
}
