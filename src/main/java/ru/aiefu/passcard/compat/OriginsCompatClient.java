package ru.aiefu.passcard.compat;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import ru.aiefu.passcard.PacketsIDs;

import java.util.ArrayList;
import java.util.Collections;

public class OriginsCompatClient {

    public static   void registerOriginsReceiver(){
        ClientPlayNetworking.registerReceiver(PacketsIDs.PASSCARD_ORIGINS_SCREEN, OriginsCompatClient::openOriginsSelectionScreen);
    }

    public static void openOriginsSelectionScreen(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender){
        boolean showDirtBackground = packetByteBuf.readBoolean();
        minecraftClient.execute(() -> {
            ArrayList<OriginLayer> layers = new ArrayList<>();
            OriginComponent component = ModComponents.ORIGIN.get(minecraftClient.player);
            OriginLayers.getLayers().forEach(layer -> {
                if(layer.isEnabled() && !component.hasOrigin(layer)) {
                    layers.add(layer);
                }
            });
            Collections.sort(layers);
            minecraftClient.openScreen(new ChooseOriginScreen(layers, 0, showDirtBackground));
        });
    }
}
