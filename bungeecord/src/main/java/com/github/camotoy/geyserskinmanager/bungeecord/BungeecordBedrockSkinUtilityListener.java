package com.github.camotoy.geyserskinmanager.bungeecord;

import com.github.camotoy.geyserskinmanager.common.BedrockPluginMessageData;
import com.github.camotoy.geyserskinmanager.common.Constants;
import com.github.camotoy.geyserskinmanager.common.RawSkin;
import com.github.camotoy.geyserskinmanager.common.SkinDatabase;
import com.github.camotoy.geyserskinmanager.common.platform.BedrockSkinUtilityListener;
import com.github.camotoy.geyserskinmanager.common.skinretriever.BedrockSkinRetriever;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class BungeecordBedrockSkinUtilityListener extends BedrockSkinUtilityListener<ProxiedPlayer> implements Listener {

    public BungeecordBedrockSkinUtilityListener(SkinDatabase database, BedrockSkinRetriever skinRetriever) {
        super(database, skinRetriever);
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        if (this.moddedPlayers.containsKey(event.getPlayer().getUniqueId())) {
            sendAllCapes(event.getPlayer());
        }

        BedrockPluginMessageData data = this.database.getPluginMessageData(event.getPlayer().getUniqueId());
        if (data != null) {
            onBedrockServerJoinOrSwitch(event.getPlayer().getServer().getInfo(), data);
        }
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if ((event.getTag().equals("minecraft:register") || event.getTag().equals("REGISTER")) && event.getSender() instanceof ProxiedPlayer) {
            String[] registeredChannels = new String(event.getData(), StandardCharsets.UTF_8).split("\0");
            for (String channel : registeredChannels) {
                if (channel.equals(Constants.MOD_PLUGIN_MESSAGE_NAME)) {
                    onModdedPlayerConfirm(((ProxiedPlayer) event.getSender()));
                    break;
                }
            }
        }
    }

    public void onBedrockPlayerJoin(ProxiedPlayer player, RawSkin skin, ServerInfo serverInfo) {
        BedrockPluginMessageData payload = getSkinAndCape(getUUID(player), skin);
        if (payload != null) {
            onBedrockServerJoinOrSwitch(serverInfo, payload);
        }
    }

    public void onBedrockServerJoinOrSwitch(ServerInfo serverInfo, BedrockPluginMessageData data) {
        for (ProxiedPlayer moddedPlayer : moddedPlayers.values()) {
            if (moddedPlayer.getServer().getInfo().getName().equals(serverInfo.getName())) {
                sendPluginMessageData(moddedPlayer, data);
            }
        }
    }

    @Override
    public void sendAllCapes(ProxiedPlayer player) {
        for (Map.Entry<UUID, BedrockPluginMessageData> data : database.getPluginMessageData()) {
            if (player.getServer().getInfo().getPlayers().contains(ProxyServer.getInstance().getPlayer(data.getKey()))) {
                sendPluginMessageData(player, data.getValue());
            }
        }
    }

    @Override
    public void sendPluginMessage(byte[] payload, ProxiedPlayer player) {
        player.sendData(Constants.MOD_PLUGIN_MESSAGE_NAME, payload);
    }

    @Override
    public UUID getUUID(ProxiedPlayer player) {
        return player.getUniqueId();
    }
}
