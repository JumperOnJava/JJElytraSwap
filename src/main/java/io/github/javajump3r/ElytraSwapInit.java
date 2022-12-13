package io.github.javajump3r;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.Vec3d;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.logging.Logger;

public class ElytraSwapInit implements ClientModInitializer {
	int lastJumpPressTick =0;
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(ElytraSwapInit::tryWearChestplate);
	}

	private Vec3d prevPos=new Vec3d(0,0,0);

	public static void tryWearChestplate(MinecraftClient client)
	{
		if(client.world==null)
			return;
		if(client.player.isOnGround())
			wearRequiredItem(false);
	}

	public static void tryWearElytra(MinecraftClient client) {
		if(client.world==null)
			return;

		var logger = LoggerFactory.getLogger("JJElytraSwap");


		var inventory = client.player.getInventory().main;
		var armor = client.player.getInventory().armor;
		var offhand = client.player.getInventory().offHand;

		boolean shouldFly=true;
		boolean haveElytra=false;
		{
			for(var slot : inventory)
			{
				if(slot.getItem()==Items.ELYTRA)
				{
					haveElytra=true;
					break;
				}
			}for(var slot : armor)
		{
			if(slot.getItem()==Items.ELYTRA)
			{
				haveElytra=true;
				break;
			}
		}
			for(var slot : offhand)
			{
				if(slot.getItem()==Items.ELYTRA)
				{
					haveElytra=true;
					break;
				}
			}
		}

		//shouldFly =

		shouldFly = shouldFly && haveElytra;
		wearRequiredItem(shouldFly);
	}
	private static void wearRequiredItem(boolean isElytra) {
		var client = MinecraftClient.getInstance();

		int elytraSlot = -1;
		int chestplateSlot = -1;
		ItemStack wearedItemStack =  client.player.getInventory().getStack(38);
		int[] range = new int[37];
		for (int i = 0; i<9; i++) range[i] = 8-i;
		for (int i = 9; i<36; i++) range[i] = 35-(i-9);
		range[36] = 40;

		for(int slot : range) {

			ItemStack stack = client.player.getInventory().getStack(slot);
			if(stack.isEmpty() || !(stack.getItem() instanceof ArmorItem || stack.getItem() instanceof ElytraItem))
				continue;
			if (stack.getItem() instanceof ElytraItem) {
				elytraSlot = slot;
				continue;
			}

			ArmorItem armorItem = (ArmorItem)stack.getItem();
			if (armorItem.getSlotType() == EquipmentSlot.CHEST) {
				chestplateSlot = slot;
			}
		}
		if(wearedItemStack.getItem()==Items.ELYTRA && !isElytra)
		{
			swap(chestplateSlot,client);
			return;
		}
		if(wearedItemStack.getItem()!=Items.ELYTRA && isElytra)
		{
			swap(elytraSlot,client);
			client.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(client.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
			client.player.startFallFlying();
			return;
		}
		if(client.player.checkFallFlying())
		{
			client.player.stopFallFlying();
		}
	}
	private static void swap(int slot, MinecraftClient client) {
		int slot2 = slot;
		if (slot2 == 40) slot2 = 45;
		if(slot2 < 9) slot2 += 36;

		client.interactionManager.clickSlot(0, slot2, 0, SlotActionType.PICKUP, client.player);

		client.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, client.player);

		client.interactionManager.clickSlot(0, slot2, 0, SlotActionType.PICKUP, client.player);
	}

}
