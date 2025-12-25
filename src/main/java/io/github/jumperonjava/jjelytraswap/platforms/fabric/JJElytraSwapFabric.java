//? if fabric {
/*package io.github.jumperonjava.jjelytraswap.platforms.fabric;

import io.github.jumperonjava.jjelytraswap.ModPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class JJElytraSwapFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JJElytraSwapInit.entrypoint(new FabricPlatform());
	}
	public static class FabricPlatform implements ModPlatform{

		@Override
		public String getModloader() {
			return "Fabric";
		}

		@Override
		public boolean isModLoaded(String modloader) {
			return FabricLoader.getInstance().isModLoaded(modloader);
		}

		@Override
		public void registerClientTickEvent(Consumer<MinecraftClient> o) {
			ClientTickEvents.END_CLIENT_TICK.register(o::accept);
		}

		@Override
		public KeyBinding registerKeyBind(String translationKeyName, int defaultKeyId) {
			//? if >= 1.21.9 {
			KeyBinding.Category kbCategory = new KeyBinding.Category(Identifier.of("jjelytraswap","generic"));
			var bind = new KeyBinding(translationKeyName,defaultKeyId,kbCategory);
			KeyBindingHelper.registerKeyBinding(bind);
			return bind;
			//?} else {
			/^var bind = new KeyBinding(translationKeyName,defaultKeyId,"JJElytraSwap");
			KeyBindingHelper.registerKeyBinding(bind);
			return bind;
			^///?}
		}
	}
}
*///?}