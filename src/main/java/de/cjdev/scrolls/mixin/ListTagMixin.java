package de.cjdev.scrolls.mixin;

import de.cjdev.scrolls.LuaUserData;
import net.minecraft.nbt.ListTag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ListTag.class)
public class ListTagMixin implements LuaUserData {
}
