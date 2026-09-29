//? if fabric {
/*package io.github.jumperonjava.jjelytraswap.platforms.fabric;

import io.github.jumperonjava.jjelytraswap.ModPlatform;
import net.fabricmc.api.ClientModInitializer;
import io.github.jumperonjava.jjelytraswap.JJElytraSwapInit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

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
		public void registerClientTickEvent(Consumer<Minecraft> o) {
			ClientTickEvents.END_CLIENT_TICK.register(o::accept);
		}

		@Override
		public KeyMapping registerKeyMap(String translationKeyName, int defaultKeyId) {
            KeyMapping.Category kbCategory = new KeyMapping.Category(Identifier.fromNamespaceAndPath("jjelytraswap", "generic"));
			var bind = new KeyMapping(translationKeyName,defaultKeyId,kbCategory);
			KeyMappingHelper.registerKeyMapping(bind);
			return bind;
		}
	}
}
*///?}