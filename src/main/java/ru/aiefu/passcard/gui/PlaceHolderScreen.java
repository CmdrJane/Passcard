package ru.aiefu.passcard.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class PlaceHolderScreen extends Screen {
    private int centerX;
    private int centerY;
    private Text message;
    private Text message2;
    private boolean isRed;

    public PlaceHolderScreen(Text message, Text message2, boolean isRed) {
        super(new LiteralText(""));
        this.message = message;
        this.message2 = message2;
        this.isRed = isRed;
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        this.centerX = width / 2;
        this.centerY = height / 2;
        super.init(client, width, height);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        drawCenteredText(matrices,textRenderer, message, centerX, centerY - 10, isRed ? 16733525 : 16777215);
        drawCenteredText(matrices,textRenderer, message2, centerX, centerY + 20, isRed ? 16733525 : 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    /**
     * Checks whether this screen should be closed when the escape key is pressed.
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
