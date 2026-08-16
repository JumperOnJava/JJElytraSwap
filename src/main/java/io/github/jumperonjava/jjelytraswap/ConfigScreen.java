package io.github.jumperonjava.jjelytraswap;

//? if >=26 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    public ConfigScreen(Screen parent) {
        super(Component.empty());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(minecraft.font, Component.literal("Hello, world"), width / 2, height / 2, 0xFFFFFFFF);
    }

    public static ConfigScreen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }
}
//?} else {
/*import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    public ConfigScreen(Screen parent) {
        super(Text.empty());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(client.textRenderer, "Hello, world", width / 2, height / 2, 0xFFFFFFFF);
    }

    public static ConfigScreen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent);
    }
}
*///?}
