package de.cjdev.scrolls;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
    public static final DataComponentType<CompoundTag> PERSISTENT_DATA = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.parse("scrolls:persistent_data"),
            DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).build()
    );

    public static void load() {
    }
}
