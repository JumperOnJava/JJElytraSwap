package io.github.jumperonjava.jjelytraswap;


import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

/**
 * This interface allows you to define platform specific code, and call it in 
 */

public interface ModPlatform {
    String getModloader();
    boolean isModLoaded(String modloader);
    void registerClientTickEvent(Consumer<Minecraft> o);

    KeyMapping registerKeyMap(String translationKeyName, int defaultKeyId);
}
