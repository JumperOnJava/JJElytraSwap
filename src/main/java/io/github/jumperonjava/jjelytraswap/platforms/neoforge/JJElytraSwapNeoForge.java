//? if neoforge {
package io.github.jumperonjava.jjelytraswap.platforms.neoforge;

import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import io.github.jumperonjava.jjelytraswap.ModPlatform;
//? if >=26 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
*///?}
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

@Mod("jjelytraswap")
public class JJElytraSwapNeoForge {
    private static IEventBus MOD_EVENT_BUS;

    public JJElytraSwapNeoForge(IEventBus eventBus) {
        MOD_EVENT_BUS = eventBus;
        JJElytraSwapInit.entrypoint(new NeoForgePlatform());
    }

    public static class NeoForgePlatform implements ModPlatform {
        @Override
        public String getModloader() {
            return "NeoForge";
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }

        //? if >=26 {
        @Override
        public void registerClientTickEvent(Consumer<Minecraft> o) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> o.accept(Minecraft.getInstance()));
        }

        @Override
        public KeyMapping registerKeyBind(String translationKeyName, int defaultKeyId) {
            KeyMapping.Category kbCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("jjelytraswap", "generic"));
            var keyBinding = new KeyMapping(translationKeyName, defaultKeyId, kbCategory);
            MOD_EVENT_BUS.addListener((RegisterKeyMappingsEvent event) -> event.register(keyBinding));
            return keyBinding;
        }
        //?} else {
        /*@Override
        public void registerClientTickEvent(Consumer<MinecraftClient> o) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> o.accept(MinecraftClient.getInstance()));
        }

        @Override
        public KeyBinding registerKeyBind(String translationKeyName, int defaultKeyId) {
            KeyBinding.Category kbCategory = new KeyBinding.Category(Identifier.of("jjelytraswap","generic"));
            var keyBinding = new KeyBinding(translationKeyName,defaultKeyId,kbCategory);
            MOD_EVENT_BUS.addListener((RegisterKeyMappingsEvent event) -> event.register(keyBinding));
            return keyBinding;
        }
        *///?}
    }
}
//?}
