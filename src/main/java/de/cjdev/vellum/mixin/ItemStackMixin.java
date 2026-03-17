package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements LuaUserData {
    @Shadow
    public abstract Item getItem();

    @Override
    public Object vellum$getField(String field) {
        return switch (field) {
            case "Id" -> this.getItem().builtInRegistryHolder().key().identifier().toShortLanguageKey();
            default -> LuaUserData.super.vellum$getField(field);
        };
    }

    @Override
    public String vellum$luaName() {
        return "ItemStack";
    }
}
