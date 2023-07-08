package io.github.javajump3r;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;

abstract class ElytraFavorability {
    public static int GOOD = 5; // Mending (2 points) + Unbreaking III (3 points)

    public static int getFavorabilityScore(ItemStack elytraItemStack) {
        if (!(elytraItemStack.getItem() instanceof ElytraItem)) {
            return 0;
        }

        int score = 0;

        score += 2 * EnchantmentHelper.getLevel(Enchantments.MENDING, elytraItemStack);
        score += EnchantmentHelper.getLevel(Enchantments.UNBREAKING, elytraItemStack);

        return score;
    }

    public static int getFavorableSlot(MinecraftClient client, int[] slotsToCheck) {
        // Find elytra with Unbreaking III and/or Mending as a priority if there are
        // multiple elytras

        int bestSlot = -1;
        int bestRank = -1;

        for (int slot : slotsToCheck) {
            ItemStack itemStackAtSlot = client.player.getInventory().getStack(slot);

            if (itemStackAtSlot.getItem() instanceof ElytraItem) {
                final int rank = ElytraFavorability.getFavorabilityScore(itemStackAtSlot);

                if (rank < bestRank) {
                    continue;
                }

                bestRank = rank;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }
}