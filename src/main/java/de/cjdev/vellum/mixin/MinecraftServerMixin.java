package de.cjdev.vellum.mixin;

import de.cjdev.vellum.LuaUserData;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements LuaUserData {
    @Override
    public String vellum$luaName() {
        return "Server";
    }
}
