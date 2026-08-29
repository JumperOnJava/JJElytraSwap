package io.github.jumperonjava.jjelytraswap;

//? if >=26 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
//?} else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
*///?}
import java.util.function.Consumer;

public interface ModPlatform {
    String getModloader();
    boolean isModLoaded(String modloader);
    //? if >=26 {
    void registerClientTickEvent(Consumer<Minecraft> o);
    KeyMapping registerKeyBind(String translationKeyName, int defaultKeyId);
    //?} else {
    /*void registerClientTickEvent(Consumer<MinecraftClient> o);
    KeyBinding registerKeyBind(String translationKeyName, int defaultKeyId);
    *///?}
}
