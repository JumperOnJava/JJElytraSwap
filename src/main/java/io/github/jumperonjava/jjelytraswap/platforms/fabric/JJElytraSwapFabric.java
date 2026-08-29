//? if fabric {
/*package io.github.jumperonjava.jjelytraswap.platforms.fabric;

import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import io.github.jumperonjava.jjelytraswap.ModPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
//? if >=26 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
//?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
//?}
import java.util.function.Consumer;

public class JJElytraSwapFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JJElytraSwapInit.entrypoint(new FabricPlatform());
	}

	public static class FabricPlatform implements ModPlatform {
		@Override
		public String getModloader() {
			return "Fabric";
		}

		@Override
		public boolean isModLoaded(String modloader) {
			return FabricLoader.getInstance().isModLoaded(modloader);
		}

		//? if >=26 {
		@Override
		public void registerClientTickEvent(Consumer<Minecraft> o) {
			ClientTickEvents.END_CLIENT_TICK.register(o::accept);
		}

		@Override
		public KeyMapping registerKeyBind(String translationKeyName, int defaultKeyId) {
			KeyMapping.Category kbCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("jjelytraswap", "generic"));
			var bind = new KeyMapping(translationKeyName, defaultKeyId, kbCategory);
			KeyMappingHelper.registerKeyMapping(bind);
			return bind;
		}
		//?} else {
		@Override
		public void registerClientTickEvent(Consumer<MinecraftClient> o) {
			ClientTickEvents.END_CLIENT_TICK.register(o::accept);
		}

		@Override
		public KeyBinding registerKeyBind(String translationKeyName, int defaultKeyId) {
			KeyBinding.Category kbCategory = new KeyBinding.Category(Identifier.of("jjelytraswap","generic"));
			var bind = new KeyBinding(translationKeyName,defaultKeyId,kbCategory);
			KeyBindingHelper.registerKeyBinding(bind);
			return bind;
		}
		//?}
	}
}
*///?}
