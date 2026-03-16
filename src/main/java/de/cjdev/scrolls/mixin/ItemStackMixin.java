package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements LuaUserData {
    @Shadow
    public abstract Item getItem();

    @Override
    public Object scrolls$getField(String field) {
        return switch (field) {
            case "Id" -> this.getItem().builtInRegistryHolder().key().identifier().toShortLanguageKey();
            default -> null;
        };
    }
}
