package io.github.jumperonjava.jjelytraswap;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

//THIS CLASS IS UNUSED

public class ConfigScreen extends Screen {

    public ConfigScreen(Screen parent) {
        super(Component.empty());
        this.addRenderableOnly(this::render);
    }

    private void render(GuiGraphicsExtractor context,  int mouseX, int mouseY, float delta) {
        context.textWithBackdrop(minecraft.font,
                Component.literal("Hello, world!"),
                width / 2,
                height / 2,
                minecraft.font.width("Hello, world!"),
                0xFFFFFFFF);
    }

    public static ConfigScreen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }
}
