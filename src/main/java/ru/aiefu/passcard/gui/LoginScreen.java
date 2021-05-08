package ru.aiefu.passcard.gui;

import com.google.crypto.tink.HybridEncrypt;
import com.google.crypto.tink.KeysetHandle;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import ru.aiefu.passcard.PacketsIDs;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class LoginScreen extends Screen {

    private int centerX;
    private int centerY;

    private PasswordWidget password;
    KeysetHandle publicKey;
    boolean isReg;
    private int timeout;
    boolean wrongPass;

    public LoginScreen(Text title, KeysetHandle handler, boolean bl, int timeout, boolean wrongPass) {
        super(title);
        this.publicKey = handler;
        this.isReg = bl;
        this.timeout = timeout;
        this.wrongPass = wrongPass;
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        this.centerX = width / 2;
        this.centerY = height / 2;
        super.init(client, width, height);
        this.password = new PasswordWidget(this.textRenderer, centerX - 90, centerY - 10, 180, 20, new TranslatableText("passcard.passfield"));
        this.password.setMaxLength(24);
        this.password.setDrawsBackground(true);
        this.password.setEditableColor(0xffffff);
        this.password.setVisible(true);
        this.password.setFocusUnlocked(true);
        this.children.add(this.password);
        this.addButton(new ButtonWidget(centerX + 94, centerY - 10, 20, 20, new LiteralText(""), action -> {
            this.password.setShowPassword(!this.password.getShowPassword());
        }));
        this.addButton(new ButtonWidget(centerX + 10, centerY + 20, 80, 20, new LiteralText(isReg ? "Register" : "Log In"), action -> {
            try {
                String pass = this.password.getText();
                HybridEncrypt hybridEncrypt = this.publicKey.getPrimitive(HybridEncrypt.class);
                byte[] byteArr = hybridEncrypt.encrypt(pass.getBytes(StandardCharsets.UTF_8), null);
                this.client.execute(() -> client.openScreen(new PlaceHolderScreen(new LiteralText("Logging in..."), new LiteralText(""), false)));
                ClientPlayNetworking.send(PacketsIDs.SEND_AUTH_REQUEST, new PacketByteBuf(Unpooled.buffer()).writeByteArray(byteArr));
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                this.client.world.disconnect();
                this.client.disconnect();
                this.onClose();
            }
        }));
        this.addButton(new ButtonWidget(centerX - 90, centerY + 20, 80, 20, new LiteralText("Exit"), action -> {
            this.client.world.disconnect();
            this.client.disconnect();
            this.onClose();
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.password.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, textRenderer, new LiteralText(this.timeout / 20 + ""), centerX, centerY - 22, 16777215);
        if(wrongPass){
            drawCenteredText(matrices, textRenderer, new LiteralText("Wrong password"), centerX, centerY - 42, 16733525);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        --this.timeout;
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
