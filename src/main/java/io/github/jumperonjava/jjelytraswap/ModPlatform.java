//? if <26.2 {
package io.github.jumperonjava.jjelytraswap;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.function.Consumer;

/**
 * This interface allows you to define platform specific code, and call it in
 */

public interface ModPlatform {
    String getModloader();
    boolean isModLoaded(String modloader);
    void registerClientTickEvent(Consumer<MinecraftClient> o);

    KeyBinding registerKeyBind(String translationKeyName, int defaultKeyId);
}
//?} else {
/*package io.github.jumperonjava.jjelytraswap;


import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

import java.util.function.Consumer;

public interface ModPlatform {
    String getModloader();
    boolean isModLoaded(String modloader);
    void registerClientTickEvent(Consumer<Minecraft> o);

    KeyMapping registerKeyBind(String translationKeyName, int defaultKeyId);
}
*///?}