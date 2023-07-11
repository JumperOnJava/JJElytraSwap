package io.github.javajump3r;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ElytraSwapInit implements ClientModInitializer {
    public static boolean enabled = true;

    public static void tryWearChestplate(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        if ( isSlotChestplate(client, 38)) {
            return;
        }

        if(FabricLoader.getInstance().isModLoaded("elytra-recast"))
            if(client.options.jumpKey.isPressed())
                return;

        //i don't know slot order lol
        if(!(client.player.getInventory().armor.get(2).getItem() == Items.ELYTRA ||
           client.player.getInventory().armor.get(1).getItem() == Items.ELYTRA))
            return;

        for (int slot : slotArray()) {
            if (isSlotChestplate(client, slot)) {
                swap(slot, client);
                return;
            }
        }
    }

    public static void tryWearElytra(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        if (client.player.getInventory().getStack(38).getItem() == Items.ELYTRA) {
            return;
        }

        var elytraSlots = getElytraSlots();

        elytraSlots.sort(Comparator.comparingInt(slot -> getElytraStat(client.player.getInventory().getStack(slot))));

        if (!elytraSlots.isEmpty()) {
            int bestSlot = elytraSlots.get(elytraSlots.size() - 1);
            wearElytra(bestSlot, client);
        }
    }

    public static List<Integer> getElytraSlots() {
        List<Integer> elytraSlots = new ArrayList<>();

        for (int slot : slotArray()) {
            if (MinecraftClient.getInstance().player.getInventory().getStack(slot).getItem() instanceof ElytraItem) {
                elytraSlots.add(slot);
            }
        }
        return elytraSlots;
    }

    private static int getElytraStat(ItemStack elytraItem) {
        return (EnchantmentHelper.getLevel(Enchantments.MENDING,elytraItem)*3+1)+EnchantmentHelper.getLevel(Enchantments.UNBREAKING,elytraItem);
    }


    private static void wearElytra(int slotId, MinecraftClient client) {
        swap(slotId, client);
        try {
            client.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(client.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            client.player.startFallFlying();
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

    public static boolean isSlotChestplate(MinecraftClient client, int slotId) {

        if (client.player == null) {
            return false;
        }
        ItemStack chestSlot = client.player.getInventory().getStack(slotId);

        return !chestSlot.isEmpty() &&
                chestSlot.getItem() instanceof ArmorItem &&
                ((ArmorItem) chestSlot.getItem()).getSlotType() == EquipmentSlot.CHEST;
    }

    private static int[] slotArray() {
        int[] range = new int[37];
        for (int i = 0; i < 9; i++) range[i] = 8 - i;
        for (int i = 9; i < 36; i++) range[i] = 35 - (i - 9);
        range[36] = 40;
        return range;
    }

    public static boolean prevTickIsOnGround=true;
    public void onInitializeClient() {
        var bind = new KeyBinding("jjelytraswap.keybind",-1,"LavaJumper");
        ClientTickEvents.END_CLIENT_TICK.register(client->{
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
            if(client.player.isOnGround()&&!prevTickIsOnGround)
                tryWearChestplate(client);
            prevTickIsOnGround=client.player.isOnGround();
        });
        KeyBindingHelper.registerKeyBinding(bind);
    }

}
