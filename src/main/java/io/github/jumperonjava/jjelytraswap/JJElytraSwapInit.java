//? if <26.2 {
package io.github.jumperonjava.jjelytraswap;

import me.lunaluna.fabric.elytrarecast.config.ElytraRecastConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.xml.crypto.Data;

//? if < 1.21.5
/*import net.minecraft.item.ArmorItem;*/


public class JJElytraSwapInit
{
	public static final String MODID = "jjelytraswap";
	public static final Logger LOGGER = LoggerFactory.getLogger("JJElytraSwap");
	public static ModPlatform PLATFORM = null;

	public static void entrypoint(ModPlatform platform) {
		JJElytraSwapInit.PLATFORM = platform;
		onInitializeClient();

	}
	public static boolean enabled = true;

	public static boolean stackHasComponent(ItemStack stack, ComponentType<?> type) {
		//? if fabric || < 1.21.5 {
		/*return stack.contains(type);
		*///?} else {
		return stack.has(type);
		//?}
	}

	public static void tryWearChestplate(MinecraftClient client) {
		if (client.world == null || client.player == null) {
			return;
		}

		if (client.player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
			return;
		}

		var chestplateSlots = getChestplateSlots();

		chestplateSlots = chestplateSlots
				.stream()
				.filter(slot->(getChestplateStat(client.player.getInventory().getStack(slot))>0f))
				.sorted(
						Comparator.comparingInt(
								slot -> getChestplateStat(
										client.player.getInventory().getStack(slot)
								)
						)
				).collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(chestplateSlots);

		//? if fabric {
		/*if(PLATFORM.isModLoaded("elytra-recast")){
			try {

				if(client.options.jumpKey.isPressed() && elytraRecastEnabled())
					return;
			}
			catch (Exception ignored){
				ignored.printStackTrace();
			}
		}
		*///?}


//		if(stackHasComponent(client.player.getEquippedStack(EquipmentSlot.CHEST),DataComponentTypes.GLIDER))
//			return;

		if (!chestplateSlots.isEmpty()) {
			int bestSlot = chestplateSlots.get(0);
			swap(bestSlot, client);
		}
	}

	private static boolean elytraRecastEnabled() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		return ElytraRecastConfig.enabled && ElytraRecastConfig.jumpEnabled;
	}

	public static void tryWearElytra() {
		if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().player == null) {
			return;
		}

		if (stackHasComponent(MinecraftClient.getInstance().player.getInventory().getStack(38),DataComponentTypes.GLIDER)) {
			return;
		}

		var elytraSlots = getElytraSlots();

		elytraSlots.sort(Comparator.comparingInt(slot -> getElytraStat(MinecraftClient.getInstance().player.getInventory().getStack(slot))));

		if (!elytraSlots.isEmpty()) {
			int bestSlot = elytraSlots.get(elytraSlots.size() - 1);
			wearElytra(bestSlot);
		}
	}

	public static List<Integer> getElytraSlots() {
		List<Integer> elytraSlots = new ArrayList<>();

		for (int slot : slotArray()) {
			if (stackHasComponent(MinecraftClient.getInstance().player.getInventory().getStack(slot),DataComponentTypes.GLIDER)) {
				elytraSlots.add(slot);
			}
		}
		return elytraSlots;
	}


	public static List<Integer> getChestplateSlots() {
		List<Integer> chestplateSlots = new ArrayList<>();
		var client = MinecraftClient.getInstance();

		for (int slot : slotArray()) {
			if (isSlotChestplate(slot)) {
				chestplateSlots.add(slot);
			}
		}

		return chestplateSlots;
	}
    private static Registry<Enchantment> getEnchantmentRegistry() {
        return MinecraftClient.getInstance().world.getRegistryManager()
        .getOrThrow(RegistryKeys.ENCHANTMENT);
	}

	private static int getLevel(RegistryKey<Enchantment> key, ItemStack stack) {
        var enchant = getEnchantmentRegistry().get(key);
		RegistryEntry<Enchantment> enchantEntry = getEnchantmentRegistry().getEntry(enchant);
		return EnchantmentHelper.getLevel(enchantEntry,stack);
	}
	private static int getElytraStat(ItemStack elytraItem) {
		var stat = (getLevel(Enchantments.MENDING,elytraItem)*3+1)+getLevel(Enchantments.UNBREAKING,elytraItem);

		return stat;
	}

	private static int getChestplateStat(ItemStack chestplateItem) {
		float score = 1;

		var holder = (ComponentHolder)chestplateItem;

		if(stackHasComponent(chestplateItem,DataComponentTypes.EQUIPPABLE)){
			if(chestplateItem.get(DataComponentTypes.EQUIPPABLE).slot()==EquipmentSlot.CHEST){
				var component = chestplateItem.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
				for (AttributeModifiersComponent.Entry entry : component.modifiers()) {
					RegistryEntry<EntityAttribute> attribute = entry.attribute();
					if(attribute == EntityAttributes.ARMOR) {
						score += entry.modifier().value();
					}
					if(attribute == EntityAttributes.ARMOR_TOUGHNESS) {
						score += entry.modifier().value();
					}
				}
				score += getLevel(Enchantments.PROTECTION,chestplateItem)*2;
				score += getLevel(Enchantments.MENDING,chestplateItem)*0.5;
				score += stackHasComponent(chestplateItem,DataComponentTypes.CUSTOM_NAME)?0.25:0;
				score += getLevel(Enchantments.UNBREAKING,chestplateItem)*0.24/3;
			}
		}

		return (int) (score*1000);
	}

	private static void wearElytra(int slotId) {
		swap(slotId, MinecraftClient.getInstance());
		try {
			//? if fabric {
			/*MinecraftClient.getInstance().getNetworkHandler().sendPacket(new ClientCommandC2SPacket(MinecraftClient.getInstance().player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
			*///?} else {
			MinecraftClient.getInstance().getNetworkHandler().send(new ClientCommandC2SPacket(MinecraftClient.getInstance().player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
			//?}

			MinecraftClient.getInstance().player.startGliding();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}


	private static void swap(int slot, MinecraftClient client) {
		int slot2 = slot;
		if (slot2 == 40) slot2 = 45;
		if (slot2 < 9) slot2 += 36;

		try {
			client.interactionManager.clickSlot(0, slot2, 0, SlotActionType.PICKUP, client.player);
			client.interactionManager.clickSlot(0, 6, 0, SlotActionType.PICKUP, client.player);
			client.interactionManager.clickSlot(0, slot2, 0, SlotActionType.PICKUP, client.player);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}
	public static boolean isSlotChestplate(int slotId) {

		if (MinecraftClient.getInstance().player == null) {
			return false;
		}
		ItemStack chestSlot = MinecraftClient.getInstance().player.getInventory().getStack(slotId);

		return !chestSlot.isEmpty() &&
				stackHasComponent(chestSlot,DataComponentTypes.EQUIPPABLE) &&
				chestSlot.get(DataComponentTypes.EQUIPPABLE).slot() == EquipmentSlot.CHEST &&
				getLevel(Enchantments.BINDING_CURSE,chestSlot) == 0;
	}

	private static int[] slotArray() {
		int[] range = new int[37];
		for (int i = 0; i < 9; i++) range[i] = 8 - i;
		for (int i = 9; i < 36; i++) range[i] = 35 - (i - 9);
		range[36] = 40;
		return range;
	}

	public static boolean shouldWearChestplatePrevTick =true;
	public static void onInitializeClient() {
		var bind = PLATFORM.registerKeyBind("jjelytraswap.keybind",-1);
		PLATFORM.registerClientTickEvent(client->{
			if (client.world == null || client.player == null) {
				return;
			}

			if(bind.wasPressed())
			{
				enabled=!enabled;
				var ts = ("jjelytraswap."+(enabled?"enabled":"disabled"));
				client.inGameHud.getChatHud().addMessage(Text.translatable(ts));
			}
			if(!enabled)
				return;
			boolean isInAir = !client.player.isOnGround() && !client.player.isInFluid();
			boolean shouldWearChestplate = !isInAir;
			if(shouldWearChestplate && !shouldWearChestplatePrevTick){
				if(stackHasComponent(MinecraftClient.getInstance().player.getEquippedStack(EquipmentSlot.CHEST),DataComponentTypes.GLIDER)){
					tryWearChestplate(client);
				}
			}
			shouldWearChestplatePrevTick = shouldWearChestplate;
		});

	}
	private static void debugLogInChat(Object... objects){
		var chat = MinecraftClient.getInstance().inGameHud.getChatHud();
		if(chat==null)
			return;
		var msg = new StringBuilder();
		for(var object : objects){
			if(object instanceof Text text){
				msg.append(text.asTruncatedString(100000));
				continue;
			}
			msg.append(object);
		}

		chat.addMessage(Text.of(msg.toString()));
	}
}
//?} else {
/*package io.github.jumperonjava.jjelytraswap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

public class JJElytraSwapInit
{
	public static final String MODID = "jjelytraswap";
	public static final Logger LOGGER = LoggerFactory.getLogger("JJElytraSwap");
	public static ModPlatform PLATFORM = null;

	public static void entrypoint(ModPlatform platform) {
		JJElytraSwapInit.PLATFORM = platform;
		onInitializeClient();
	}
	public static boolean enabled = true;

	public static boolean stackHasComponent(ItemStack stack, DataComponentType<?> type) {
		return stack.has(type);
	}

	public static void tryWearChestplate(Minecraft client) {
		if (client.level == null || client.player == null) {
			return;
		}

		if (client.player.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
			return;
		}

		var chestplateSlots = getChestplateSlots();

		chestplateSlots = chestplateSlots
				.stream()
				.filter(slot->(getChestplateStat(client.player.getInventory().getItem(slot))>0f))
				.sorted(
						Comparator.comparingInt(
								slot -> getChestplateStat(
										client.player.getInventory().getItem(slot)
								)
						)
				).collect(Collectors.toCollection(ArrayList::new));
		Collections.reverse(chestplateSlots);

		if (!chestplateSlots.isEmpty()) {
			int bestSlot = chestplateSlots.get(0);
			swap(bestSlot, client);
		}
	}

	public static void tryWearElytra() {
		if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
			return;
		}

		if (stackHasComponent(Minecraft.getInstance().player.getInventory().getItem(38),DataComponents.GLIDER)) {
			return;
		}

		var elytraSlots = getElytraSlots();

		elytraSlots.sort(Comparator.comparingInt(slot -> getElytraStat(Minecraft.getInstance().player.getInventory().getItem(slot))));

		if (!elytraSlots.isEmpty()) {
			int bestSlot = elytraSlots.get(elytraSlots.size() - 1);
			wearElytra(bestSlot);
		}
	}

	public static List<Integer> getElytraSlots() {
		List<Integer> elytraSlots = new ArrayList<>();

		for (int slot : slotArray()) {
			if (stackHasComponent(Minecraft.getInstance().player.getInventory().getItem(slot),DataComponents.GLIDER)) {
				elytraSlots.add(slot);
			}
		}
		return elytraSlots;
	}


	public static List<Integer> getChestplateSlots() {
		List<Integer> chestplateSlots = new ArrayList<>();
		var client = Minecraft.getInstance();

		for (int slot : slotArray()) {
			if (isSlotChestplate(slot)) {
				chestplateSlots.add(slot);
			}
		}

		return chestplateSlots;
	}
    private static Registry<Enchantment> getEnchantmentRegistry() {
        return Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
	}

	private static int getLevel(ResourceKey<Enchantment> key, ItemStack stack) {
		Holder<Enchantment> enchantEntry = getEnchantmentRegistry().get(key.identifier()).orElseThrow();
		return EnchantmentHelper.getItemEnchantmentLevel(enchantEntry, stack);
	}
	private static int getElytraStat(ItemStack elytraItem) {
		var stat = (getLevel(Enchantments.MENDING,elytraItem)*3+1)+getLevel(Enchantments.UNBREAKING,elytraItem);

		return stat;
	}

	private static int getChestplateStat(ItemStack chestplateItem) {
		float score = 1;

		if(stackHasComponent(chestplateItem,DataComponents.EQUIPPABLE)){
			if(chestplateItem.get(DataComponents.EQUIPPABLE).slot()==EquipmentSlot.CHEST){
				var component = chestplateItem.get(DataComponents.ATTRIBUTE_MODIFIERS);
				for (ItemAttributeModifiers.Entry entry : component.modifiers()) {
					Holder<Attribute> attribute = entry.attribute();
					if(attribute == Attributes.ARMOR) {
						score += entry.modifier().amount();
					}
					if(attribute == Attributes.ARMOR_TOUGHNESS) {
						score += entry.modifier().amount();
					}
				}
				score += getLevel(Enchantments.PROTECTION,chestplateItem)*2;
				score += getLevel(Enchantments.MENDING,chestplateItem)*0.5;
				score += stackHasComponent(chestplateItem,DataComponents.CUSTOM_NAME)?0.25:0;
				score += getLevel(Enchantments.UNBREAKING,chestplateItem)*0.24/3;
			}
		}

		return (int) (score*1000);
	}

	private static void wearElytra(int slotId) {
		swap(slotId, Minecraft.getInstance());
		try {
			Minecraft.getInstance().getConnection().send(new ServerboundPlayerCommandPacket(Minecraft.getInstance().player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
			Minecraft.getInstance().player.startFallFlying();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}


	private static void swap(int slot, Minecraft client) {
		int slot2 = slot;
		if (slot2 == 40) slot2 = 45;
		if (slot2 < 9) slot2 += 36;

		try {
			client.gameMode.handleContainerInput(0, slot2, 0, ContainerInput.PICKUP, client.player);
			client.gameMode.handleContainerInput(0, 6, 0, ContainerInput.PICKUP, client.player);
			client.gameMode.handleContainerInput(0, slot2, 0, ContainerInput.PICKUP, client.player);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}
	public static boolean isSlotChestplate(int slotId) {

		if (Minecraft.getInstance().player == null) {
			return false;
		}
		ItemStack chestSlot = Minecraft.getInstance().player.getInventory().getItem(slotId);

		return !chestSlot.isEmpty() &&
				stackHasComponent(chestSlot,DataComponents.EQUIPPABLE) &&
				chestSlot.get(DataComponents.EQUIPPABLE).slot() == EquipmentSlot.CHEST &&
				getLevel(Enchantments.BINDING_CURSE,chestSlot) == 0;
	}

	private static int[] slotArray() {
		int[] range = new int[37];
		for (int i = 0; i < 9; i++) range[i] = 8 - i;
		for (int i = 9; i < 36; i++) range[i] = 35 - (i - 9);
		range[36] = 40;
		return range;
	}

	public static boolean shouldWearChestplatePrevTick =true;
	public static void onInitializeClient() {
		var bind = PLATFORM.registerKeyBind("jjelytraswap.keybind",-1);
		PLATFORM.registerClientTickEvent(client->{
			if (client.level == null || client.player == null) {
				return;
			}

			if(bind.consumeClick())
			{
				enabled=!enabled;
				var ts = ("jjelytraswap."+(enabled?"enabled":"disabled"));
				client.gui.hud.getChat().addClientSystemMessage(Component.translatable(ts));
			}
			if(!enabled)
				return;
			boolean isInAir = !client.player.onGround() && !(client.player.isInWater() || client.player.isInLava());
			boolean shouldWearChestplate = !isInAir;
			if(shouldWearChestplate && !shouldWearChestplatePrevTick){
				if(stackHasComponent(Minecraft.getInstance().player.getItemBySlot(EquipmentSlot.CHEST),DataComponents.GLIDER)){
					tryWearChestplate(client);
				}
			}
			shouldWearChestplatePrevTick = shouldWearChestplate;
		});

	}
}
*///?}