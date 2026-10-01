package io.github.jumperonjava.jjelytraswap;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class JJElytraSwapInit {
    public static final String MODID = "jjelytraswap";
    public static final Logger LOGGER = LoggerFactory.getLogger("JJElytraSwap");
    public static ModPlatform PLATFORM = null;

    public static void entrypoint(ModPlatform platform) {
        JJElytraSwapInit.PLATFORM = platform;
        onInitializeClient();
    }

    public static List<Runnable> nextTickTask = new ArrayList<>();

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
                .filter(slot -> (getChestplateStat(client.player.getInventory().getItem(slot)) > 0f))
                .sorted(
                        Comparator.comparingInt(
                                slot -> getChestplateStat(
                                        client.player.getInventory().getItem(slot)
                                )
                        )
                ).collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(chestplateSlots);

            try {
                if (client.options.keyJump.isDown() && elytraRecastEnabled())
                    return;
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

        if (!chestplateSlots.isEmpty()) {
            int bestSlot = chestplateSlots.get(0);
            swap(bestSlot, client);
        }
    }

    private static boolean elytraRecastEnabled() {
        if (PLATFORM.isModLoaded("elytra-recast")) {
        }
        return false;
    }

    public static void tryWearElytra() {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) {
            return;
        }

        if (stackHasComponent(Minecraft.getInstance().player.getInventory().getItem(38), DataComponents.GLIDER)) {
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
            if (stackHasComponent(Minecraft.getInstance().player.getInventory().getItem(slot), DataComponents.GLIDER)) {
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
        return Minecraft.getInstance().level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);
    }

    private static int getLevel(ResourceKey<Enchantment> key, ItemStack stack) {
        var enchant = getEnchantmentRegistry().get(key);
        Holder<Enchantment> enchantEntry = getEnchantmentRegistry().getOrThrow(enchant.get().key());
        return EnchantmentHelper.getItemEnchantmentLevel(enchantEntry, stack);
    }

    private static int getElytraStat(ItemStack elytraItem) {
        var stat = (getLevel(Enchantments.MENDING, elytraItem) * 3 + 1) + getLevel(Enchantments.UNBREAKING, elytraItem);

        return stat;
    }

    private static int getChestplateStat(ItemStack chestplateItem) {
        float score = 1;

        var holder = (DataComponentHolder) chestplateItem;

        if (stackHasComponent(chestplateItem, DataComponents.EQUIPPABLE)) {
            if (chestplateItem.get(DataComponents.EQUIPPABLE).slot() == EquipmentSlot.CHEST) {
                var component = chestplateItem.get(DataComponents.ATTRIBUTE_MODIFIERS);
                for (ItemAttributeModifiers.Entry entry : component.modifiers()) {
                    Holder<Attribute> attribute = entry.attribute();
                    if (attribute == Attributes.ARMOR) {
                        score += entry.modifier().amount();
                    }
                    if (attribute == Attributes.ARMOR_TOUGHNESS) {
                        score += entry.modifier().amount();
                    }
                }
                score += getLevel(Enchantments.PROTECTION, chestplateItem) * 2;
                score += getLevel(Enchantments.MENDING, chestplateItem) * 0.5;
                score += stackHasComponent(chestplateItem, DataComponents.CUSTOM_NAME) ? 0.25 : 0;
                score += getLevel(Enchantments.UNBREAKING, chestplateItem) * 0.24 / 3;
            }
        }

        return (int) (score * 1000);
    }

    private static void wearElytra(int slotId) {
        swap(slotId, Minecraft.getInstance());

        Minecraft.getInstance().player.startFallFlying();

        //this is little hack
        //neo handles packets weirdly and/or does some checks that are missing in vanilla/fabric
        //and this cancels fallflying state
        //to fix this we delay flying packet for a few ticks so neo can check when we 100% have elytra in inventory
        //? if neoforge && >=26.1
        //nextTickTask.add(() -> { nextTickTask.add(() -> {
                Minecraft.getInstance().getConnection().send(new ServerboundPlayerCommandPacket(Minecraft.getInstance().player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        //? if neoforge && >=26.1
        //});});
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
                stackHasComponent(chestSlot, DataComponents.EQUIPPABLE) &&
                chestSlot.get(DataComponents.EQUIPPABLE).slot() == EquipmentSlot.CHEST &&
                getLevel(Enchantments.BINDING_CURSE, chestSlot) == 0;
    }

    private static int[] slotArray() {
        int[] range = new int[37];
        for (int i = 0; i < 9; i++) range[i] = 8 - i;
        for (int i = 9; i < 36; i++) range[i] = 35 - (i - 9);
        range[36] = 40;
        return range;
    }

    public static boolean shouldWearChestplatePrevTick = true;

    public static void onInitializeClient() {
        var bind = PLATFORM.registerKeyMap("jjelytraswap.keybind", InputConstants.UNKNOWN.getValue());
        PLATFORM.registerClientTickEvent(client -> {
            if (client.level == null || client.player == null) {
                return;
            }

            var copy = new ArrayList<>(nextTickTask);
            copy.forEach(Runnable::run);
            nextTickTask.removeAll(copy);

            if (bind.consumeClick()) {
                enabled = !enabled;
                var ts = ("jjelytraswap." + (enabled ? "enabled" : "disabled"));
                chat().addClientSystemMessage(Component.translatable(ts));
            }
            if (!enabled)
                return;
            boolean isInAir = !client.player.onGround() && !client.player.isInLiquid();
            boolean shouldWearChestplate = !isInAir;
            if (shouldWearChestplate && !shouldWearChestplatePrevTick) {
                assert Minecraft.getInstance().player != null;
                if (stackHasComponent(Minecraft.getInstance().player.getItemBySlot(EquipmentSlot.CHEST), DataComponents.GLIDER)) {
                    tryWearChestplate(client);
                }
            }
            shouldWearChestplatePrevTick = shouldWearChestplate;

        });

    }

    private static void debugLogInChat(Object... objects) {
        var msg = new StringBuilder();
        for (var object : objects) {
            if (object instanceof Component text) {
                msg.append(text.getString(100000));
                continue;
            }
            msg.append(object);
        }

        chat().addClientSystemMessage(Component.literal(msg.toString()));
    }

    private static ChatComponent chat() {
        return Minecraft.getInstance().gui
                //? if >= 26.2 {
                /*.hud
                *///? }
                .getChat();

    }
}