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

package com.ars3ne.eventos.bungeecord;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.HashMap;

// TODO: Provavelmente refazer todo esse sistema.

public class aEventosBungeecord extends Plugin implements Listener {

    // Peço desculpas por esse código horrível, mas foi isso que consegui fazer com o pouco tempo que tenho.
    // Caso você queria tentar arrumar essa atrocidade, sinta-se livre para fazer um pull request.

    private final HashMap<String, String> players_origins = new HashMap<>();

    @Override
    public void onEnable() {
        this.getProxy().registerChannel("aeventos:channel");
        this.getProxy().getPluginManager().registerListener(this,this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {

        if (!e.getTag().equals("aeventos:channel")) return;
        if (!(e.getSender() instanceof Server)) return;

        ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
        DataInputStream in = new DataInputStream(stream);


        try {

            String subchannel = in.readUTF();

            if(subchannel.equals("start")){ // Inicie o evento.

                players_origins.clear();

                String type = in.readUTF();
                String config = in.readUTF();
                String[] servers = in.readUTF().replace("[", "").replace("]", "").split(", ");

                // Mande a mensagem para iniciar o evento nos servidores.
                for(String server: servers) {

                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(server);
                    if(serv == null) continue;
                    if(serv.getPlayers().size() == 0) continue; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("start");
                    out.writeUTF(type);
                    out.writeUTF(config);

                    serv.sendData("aeventos:channel", st.toByteArray());
                }

            }

            if(subchannel.equals("stop")){ // Pare o evento.

                String srv = in.readUTF();
                String reason = in.readUTF();
                String[] servers = in.readUTF().replace("[", "").replace("]", "").split(", ");
                boolean send_to_default = in.readBoolean();
                String default_server = in.readUTF();

                if(reason.equals("noplayers") && !srv.equals(((Server) e.getSender()).getInfo().getName())) return;
                if(reason.equals("noguilds") && !srv.equals(((Server) e.getSender()).getInfo().getName())) return;

                // Mande os jogadores de volta para o servidor de origem.
                for(String player: players_origins.keySet()) {
                    if(!players_origins.get(player).equals(((Server) e.getSender()).getInfo().getName())) {
                        if(send_to_default) {
                            getProxy().getPlayer(player).connect(getProxy().getServerInfo(default_server));
                        }else {
                            getProxy().getPlayer(player).connect(getProxy().getServerInfo(players_origins.get(player)));
                        }
                    }
                }

                // Mande a mensagem para iniciar o evento nos servidores.
                for(String server: servers) {

                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(server);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("stop");
                    out.writeUTF(reason);

                    serv.sendData("aeventos:channel", st.toByteArray());
                }

            }

            if(subchannel.equals("starting")) { // Feche o evento. Última tentativa de iniciar o evento em servidores anteriormente vazios.

                String type = in.readUTF();
                String config = in.readUTF();
                String[] servers = in.readUTF().replace("[", "").replace("]", "").split(", ");

                // Mande a mensagem para iniciar o evento nos servidores.
                for(String server: servers) {

                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(server);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("starting");
                    out.writeUTF(type);
                    out.writeUTF(config);

                    serv.sendData("aeventos:channel", st.toByteArray());
                }

            }

            if(subchannel.equals("join")) { // Feche o evento. Última tentativa de iniciar o evento em servidores anteriormente vazios.

                String player = in.readUTF();
                String srv = in.readUTF();
                String[] servers = in.readUTF().replace("[", "").replace("]", "").split(", ");
                boolean send_to_default = in.readBoolean();
                String default_server = in.readUTF();

                // Adicione o jogador a hashmap de origem.
                if(send_to_default) {
                    players_origins.put(player, default_server);
                }else {
                    players_origins.put(player, getProxy().getPlayer(player).getServer().getInfo().getName());
                }

                // Se o servidor atual do jogador for diferente do atual, então o mande para o servidor.
                if(srv.equals(((Server) e.getSender()).getInfo().getName())) {

                    // Mande a mensagem para o servidor e retorne.
                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(srv);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("join");
                    out.writeUTF(player);

                    serv.sendData("aeventos:channel", st.toByteArray());

                    return;
                }
                getProxy().getPlayer(player).connect(getProxy().getServerInfo(srv));

                // Mande a mensagem para iniciar o evento nos servidores.
                for(String server: servers) {

                    //if(server.equals(srv)) continue; // Não mande a mensagem para o mesmo servidor que a enviou.

                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(server);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("join");
                    out.writeUTF(player);

                    serv.sendData("aeventos:channel", st.toByteArray());
                }

            }

            if(subchannel.equals("leave")) { // Feche o evento. Última tentativa de iniciar o evento em servidores anteriormente vazios.

                String player = in.readUTF();
                String srv = in.readUTF();
                String[] servers = in.readUTF().replace("[", "").replace("]", "").split(", ");
                boolean send_to_default = in.readBoolean();
                String default_server = in.readUTF();

                if(!players_origins.containsKey(player)) return;
                // Se o servidor atual do jogador for diferente do atual, então o mande para o servidor.
                if(players_origins.get(player).equals(((Server) e.getSender()).getInfo().getName())) {

                    // Mande a mensagem para o servidor e retorne.
                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(srv);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("leave");
                    out.writeUTF(player);

                    serv.sendData("aeventos:channel", st.toByteArray());

                    return;
                }

                if(send_to_default) {
                    getProxy().getPlayer(player).connect(getProxy().getServerInfo(default_server));
                }else {
                    getProxy().getPlayer(player).connect(getProxy().getServerInfo(players_origins.get(player)));
                }

                players_origins.remove(player);

                // Mande a mensagem para iniciar o evento nos servidores.
                for(String server: servers) {

                    //if(server.equals(srv)) continue; // Não mande a mensagem para o mesmo servidor que a enviou.

                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(server);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("leave");
                    out.writeUTF(player);

                    serv.sendData("aeventos:channel", st.toByteArray());
                }

            }

            if(subchannel.equals("spectate")) { // Feche o evento. Última tentativa de iniciar o evento em servidores anteriormente vazios.

                String player = in.readUTF();
                String srv = in.readUTF();
                String[] servers = in.readUTF().replace("[", "").replace("]", "").split(", ");
                boolean send_to_default = in.readBoolean();
                String default_server = in.readUTF();

                // Se o servidor atual do jogador for diferente do atual, então o mande para o servidor.
                if(srv.equals(((Server) e.getSender()).getInfo().getName())) {

                    // Mande a mensagem para o servidor e retorne.
                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(srv);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("spectate");
                    out.writeUTF(player);

                    serv.sendData("aeventos:channel", st.toByteArray());

                    return;
                }

                getProxy().getPlayer(player).connect(getProxy().getServerInfo(srv));

                if(send_to_default) {
                    // Adicione o jogador a hashmap de origem.
                    players_origins.put(player, default_server);
                }else {
                    // Adicione o jogador a hashmap de origem.
                    players_origins.put(player, getProxy().getPlayer(player).getServer().getInfo().getName());
                }

                // Mande a mensagem para iniciar o evento nos servidores.
                for(String server: servers) {

                    //if(server.equals(srv)) continue; // Não mande a mensagem para o mesmo servidor que a enviou.

                    ByteArrayOutputStream st = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(st);

                    ServerInfo serv = getProxy().getServerInfo(server);
                    if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                    out.writeUTF("spectate");
                    out.writeUTF(player);

                    serv.sendData("aeventos:channel", st.toByteArray());
                }

            }

            if(subchannel.equals("execute")) { // Feche o evento. Última tentativa de iniciar o evento em servidores anteriormente vazios.

                String player = in.readUTF();
                String command = in.readUTF();
                boolean send_to_default = in.readBoolean();
                String default_server = in.readUTF();

                if(!players_origins.containsKey(player)) return;

                // Mande a mensagem para o servidor e retorne.
                ByteArrayOutputStream st = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(st);

                ServerInfo serv;
                if(send_to_default) {
                    serv = getProxy().getServerInfo(default_server);
                }else {
                    serv = getProxy().getServerInfo(players_origins.get(player));
                }
                //if(serv.getPlayers().size() == 0) return; // Se o servidor não tiver jogadores online, retorne.

                out.writeUTF("execute");
                out.writeUTF(command);

                serv.sendData("aeventos:channel", st.toByteArray());

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void sendMessage(String message, ServerInfo server) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.sendData("aeventos:channel", stream.toByteArray());
    }

}
