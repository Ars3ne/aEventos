/*
 *
 * This file is part of aEventos, licensed under the MIT License.
 *
 * Copyright (c) Ars3ne
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ars3ne.eventos.hooks;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.utils.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BungeecordHook implements PluginMessageListener {

    // Peço desculpas por esse código horrível, mas foi isso que consegui fazer com o pouco tempo que tenho.
    // Caso você queria tentar arrumar essa atrocidade, sinta-se livre para fazer um pull request.

    private static final List<String> join = new ArrayList<>();
    private static final List<String> spectate = new ArrayList<>();

    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {

        if (!channel.equals("aeventos:channel")) {
            return;
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);

        try {

            String subchannel = in.readUTF();

            if(subchannel.equals("start")){ // Inicie o evento.

                String type = in.readUTF();
                String config = in.readUTF();
                if(aEventos.getEventoManager().getEvento() == null) {
                    aEventos.getEventoManager().startEvento(EventoType.getEventoType(type), ConfigFile.get(config));
                }

            }

            if(subchannel.equals("starting")){ // Feche o evento. Última tentativa de iniciar o evento em servidores anteriormente vazios.

                String type = in.readUTF();
                String config = in.readUTF();

                if(aEventos.getEventoManager().getEvento() == null) {
                    aEventos.getEventoManager().startEvento(EventoType.getEventoType(type), ConfigFile.get(config));
                }
                aEventos.getEventoManager().getEvento().startBungeecord();

            }

            if(subchannel.equals("stop")){ // Pare o evento.

                YamlConfiguration config;
                String reason = in.readUTF();

                if(aEventos.getEventoManager().getEvento() != null) {

                    config = aEventos.getEventoManager().getEvento().getConfig();
                    aEventos.getEventoManager().getEvento().stop();

                    if(reason.equals("cancelled")) {
                        List<String> broadcast_messages = config.getStringList("Messages.Cancelled");
                        for(String s : broadcast_messages) {
                            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                        }
                    }

                    if(reason.equals("noplayers")) {
                        List<String> broadcast_messages = config.getStringList("Messages.No players");
                        for(String s : broadcast_messages) {
                            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                        }
                    }

                    if(reason.equals("noguilds")) {
                        List<String> no_guild = config.getStringList("Messages.No guilds");
                        for(String s : no_guild) {
                            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                        }
                    }

                }

            }

            if(subchannel.equals("join")) {

                String player_args = in.readUTF();

                Player bukkit_player = Bukkit.getPlayer(player_args);

                if(aEventos.getEventoManager().getEvento() == null) return;
                if(aEventos.getEventoManager().getEvento().getPlayers().contains(bukkit_player)) return;
                if(aEventos.getEventoManager().getEvento().isOpen()) {
                    if(bukkit_player != null) {
                        aEventos.getEventoManager().getEvento().join(bukkit_player);
                    }else {
                        join.add(player_args);
                    }
                }

            }

            if(subchannel.equals("leave")) {

                String player_args = in.readUTF();

                Player bukkit_player = Bukkit.getPlayer(player_args);
                if(aEventos.getEventoManager().getEvento() == null) return;
                if(!aEventos.getEventoManager().getEvento().getPlayers().contains(bukkit_player)) return;
                aEventos.getEventoManager().getEvento().leave(bukkit_player);

            }

            if(subchannel.equals("spectate")) {

                String player_args = in.readUTF();

                Player bukkit_player = Bukkit.getPlayer(player_args);
                if(aEventos.getEventoManager().getEvento() == null) return;
                if(aEventos.getEventoManager().getEvento().getPlayers().contains(bukkit_player)) return;
                if(bukkit_player != null) {
                    aEventos.getEventoManager().getEvento().spectate(bukkit_player);
                }else {
                    spectate.add(player_args);
                }

            }

            if(subchannel.equals("execute")) {
                String command = in.readUTF();
                aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), command);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void startEvento(String type, String config) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF("start");
            out.writeUTF(type);
            out.writeUTF(config);
            out.writeUTF(aEventos.getInstance().getConfig().getStringList("Bungeecord.Servers").toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        aEventos.getInstance().getServer().sendPluginMessage(aEventos.getInstance(), "aeventos:channel", stream.toByteArray());
    }

    public static void startingEvento(String type, String config) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF("starting");
            out.writeUTF(type);
            out.writeUTF(config);
            out.writeUTF(aEventos.getInstance().getConfig().getStringList("Bungeecord.Servers").toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        aEventos.getInstance().getServer().sendPluginMessage(aEventos.getInstance(), "aeventos:channel", stream.toByteArray());
    }

    public static void stopEvento(String reason) {

        if(aEventos.getEventoManager().getEvento() == null) return;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF("stop");
            out.writeUTF(aEventos.getEventoManager().getEvento().getConfig().getString("Locations.Server"));
            out.writeUTF(reason);
            out.writeUTF(aEventos.getInstance().getConfig().getStringList("Bungeecord.Servers").toString());
            out.writeBoolean(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Send to default"));
            out.writeUTF(aEventos.getInstance().getConfig().getString("Bungeecord.Default"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        aEventos.getInstance().getServer().sendPluginMessage(aEventos.getInstance(), "aeventos:channel", stream.toByteArray());
    }

    public static void joinEvento(String player) {

        if(aEventos.getEventoManager().getEvento() == null) return;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF("join");
            out.writeUTF(player);
            out.writeUTF(aEventos.getEventoManager().getEvento().getConfig().getString("Locations.Server"));
            out.writeUTF(aEventos.getInstance().getConfig().getStringList("Bungeecord.Servers").toString());
            out.writeBoolean(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Send to default"));
            out.writeUTF(aEventos.getInstance().getConfig().getString("Bungeecord.Default"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        aEventos.getInstance().getServer().sendPluginMessage(aEventos.getInstance(), "aeventos:channel", stream.toByteArray());
    }

    public static void leaveEvento(String player) {

        if(aEventos.getEventoManager().getEvento() == null) return;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF("leave");
            out.writeUTF(player);
            out.writeUTF(aEventos.getEventoManager().getEvento().getConfig().getString("Locations.Server"));
            out.writeUTF(aEventos.getInstance().getConfig().getStringList("Bungeecord.Servers").toString());
            out.writeBoolean(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Send to default"));
            out.writeUTF(aEventos.getInstance().getConfig().getString("Bungeecord.Default"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        aEventos.getInstance().getServer().sendPluginMessage(aEventos.getInstance(), "aeventos:channel", stream.toByteArray());
    }

    public static void spectateEvento(String player) {

        if(aEventos.getEventoManager().getEvento() == null) return;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF("spectate");
            out.writeUTF(player);
            out.writeUTF(aEventos.getEventoManager().getEvento().getConfig().getString("Locations.Server"));
            out.writeUTF(aEventos.getInstance().getConfig().getStringList("Bungeecord.Servers").toString());
            out.writeBoolean(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Send to default"));
            out.writeUTF(aEventos.getInstance().getConfig().getString("Bungeecord.Default"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        aEventos.getInstance().getServer().sendPluginMessage(aEventos.getInstance(), "aeventos:channel", stream.toByteArray());
    }

    public static void executeCommand(String player, String command, String server) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF("execute");
            out.writeUTF(player);
            out.writeUTF(command);
            out.writeBoolean(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Commands on default"));
            out.writeUTF(String.valueOf(aEventos.getInstance().getConfig().getString("Bungeecord.Default")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        aEventos.getInstance().getServer().sendPluginMessage(aEventos.getInstance(), "aeventos:channel", stream.toByteArray());
    }

    public static List<String> getJoin() {
        return join;
    }

    public static List<String> getSpectate() {
        return spectate;
    }

}
