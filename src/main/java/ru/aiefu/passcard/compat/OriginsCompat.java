package ru.aiefu.passcard.compat;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.power.PowerTypeRegistry;
import io.github.apace100.origins.power.factory.PowerFactory;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.aiefu.passcard.PacketsIDs;

import java.util.List;

public class OriginsCompat {

    public static void openOriginsScreen(ServerPlayerEntity player){
        OriginComponent component = ModComponents.ORIGIN.get(player);

        PacketByteBuf powerListData = new PacketByteBuf(Unpooled.buffer());
        powerListData.writeInt(PowerTypeRegistry.size());
        PowerTypeRegistry.entries().forEach((entry) -> {
            PowerType<?> type = entry.getValue();
            PowerFactory.Instance factory = type.getFactory();
            if(factory != null) {
                powerListData.writeIdentifier(entry.getKey());
                factory.write(powerListData);
                powerListData.writeString(type.getOrCreateNameTranslationKey());
                powerListData.writeString(type.getOrCreateDescriptionTranslationKey());
                powerListData.writeBoolean(type.isHidden());
            }
        });

        PacketByteBuf originListData = new PacketByteBuf(Unpooled.buffer());
        originListData.writeInt(OriginRegistry.size() - 1);
        OriginRegistry.entries().forEach((entry) -> {
            if(entry.getValue() != Origin.EMPTY) {
                originListData.writeIdentifier(entry.getKey());
                entry.getValue().write(originListData);
            }
        });

        PacketByteBuf originLayerData = new PacketByteBuf(Unpooled.buffer());
        originLayerData.writeInt(OriginLayers.size());
        OriginLayers.getLayers().forEach((layer) -> {
            layer.write(originLayerData);
            if(layer.isEnabled()) {
                if(!component.hasOrigin(layer)) {
                    component.setOrigin(layer, Origin.EMPTY);
                }
            }
        });

        ServerPlayNetworking.send(player, ModPackets.POWER_LIST, powerListData);
        ServerPlayNetworking.send(player, ModPackets.ORIGIN_LIST, originListData);
        ServerPlayNetworking.send(player, ModPackets.LAYER_LIST, originLayerData);

        List<ServerPlayerEntity> playerList = player.getServer().getPlayerManager().getPlayerList();
        playerList.forEach(spe -> ModComponents.ORIGIN.syncWith(spe, ComponentProvider.fromEntity(player)));
        OriginComponent.sync(player);
        if(!component.hasAllOrigins()) {
            if(component.checkAutoChoosingLayers(player, true)) {
                component.sync();
            }
            if(component.hasAllOrigins()) {
                component.getOrigins().values().forEach(o -> {
                    o.getPowerTypes().forEach(powerType -> component.getPower(powerType).onChosen(false));
                });
            } else {
                PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
                data.writeBoolean(true);
                ServerPlayNetworking.send(player, PacketsIDs.PASSCARD_ORIGINS_SCREEN, data);
            }
        }
        if(component.hasAllOrigins()){
            ServerPlayNetworking.send(player, PacketsIDs.CLOSE_ALL_SCREENS, new PacketByteBuf(Unpooled.buffer()));
        }
    }
}
