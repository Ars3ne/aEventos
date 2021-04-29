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

package com.ars3ne.eventos.listeners;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.commands.EventoCommand;
import com.ars3ne.eventos.hooks.BungeecordHook;
import com.ars3ne.eventos.utils.EventoConfigFile;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EventoListener implements Listener {

    // Bloquear comandos nos eventos
    @EventHandler(priority = EventPriority.LOW)
    public void onCommand(PlayerCommandPreprocessEvent e) {

        // Se não está ocorrendo um evento, ou o jogador não está nele, retorne.
        if(aEventos.getEventoManager().getEvento() == null) return;
        if(e.getPlayer().hasPermission("aeventos.admin")) return;

        if(!aEventos.getEventoManager().getEvento().getPlayers().contains(e.getPlayer())
                || aEventos.getEventoManager().getEvento().getSpectators().contains(e.getPlayer())) return;

        // Carregue a lista de comandos permitidos. Se não estiver na lista, cancele o evento e envie uma mensagem.
        List<String> allowed_commands = aEventos.getInstance().getConfig().getStringList("Allowed commands");

        if(!allowed_commands.contains(e.getMessage().toLowerCase().split(" ")[0])) {
            e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Blocked command").replace("&", "§"));
            e.setCancelled(true);
        }

    }

    // Remover jogador do evento se ele saiu do servidor.
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {

        if(aEventos.getEventoManager().getEvento() == null && aEventos.getEventoChatManager().getEvento() == null) return;

        if(aEventos.getEventoChatManager().getEvento() != null) {
            aEventos.getEventoChatManager().getEvento().leave(e.getPlayer());
        }else {
            // Se o usuário estava no evento, remova-o.
            if(aEventos.getEventoManager().getEvento().getPlayers().contains(e.getPlayer())) {
                aEventos.getEventoManager().getEvento().leave(e.getPlayer());
            }else if(aEventos.getEventoManager().getEvento().getSpectators().contains(e.getPlayer())) {
                // Se o usuário estava no modo espectador, remova-o.
                aEventos.getEventoManager().getEvento().remove(e.getPlayer());
                aEventos.getEventoManager().getEvento().leaveBungeecord(e.getPlayer());
            }
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if(aEventos.getEventoManager().getEvento() == null) return;

        if(BungeecordHook.getJoin().contains(e.getPlayer().getName())) {
            aEventos.getEventoManager().getEvento().join(e.getPlayer());
            BungeecordHook.getJoin().remove(e.getPlayer().getName());
        }

        if(BungeecordHook.getSpectate().contains(e.getPlayer().getName())) {
            if(!aEventos.getEventoManager().getEvento().isSpectatorAllowed()) return;
            aEventos.getEventoManager().getEvento().spectate(e.getPlayer());
            BungeecordHook.getSpectate().remove(e.getPlayer().getName());
        }

    }

    // Listeners do modo setup

    // Ao escrever uma placa
    @EventHandler
    public void onSignChange(SignChangeEvent e) {

        Map<Player, YamlConfiguration> setup = EventoCommand.getSetupList();

        if(!setup.containsKey(e.getPlayer())) return;
        if(setup.get(e.getPlayer()).getStringList("Messages.Sign") == null) return;

        if(e.getLine(0).equalsIgnoreCase("[evento]")) {

            if(e.getLine(1).equalsIgnoreCase("vitoria")) {

                e.setLine(0, setup.get(e.getPlayer()).getStringList("Messages.Sign").get(0).replace("&", "§"));
                e.setLine(1, setup.get(e.getPlayer()).getStringList("Messages.Sign").get(1).replace("&", "§"));
                e.setLine(2, setup.get(e.getPlayer()).getStringList("Messages.Sign").get(2).replace("&", "§"));
                e.setLine(3, setup.get(e.getPlayer()).getStringList("Messages.Sign").get(3).replace("&", "§"));

            }

            if(e.getLine(1).equalsIgnoreCase("checkpoint")) {

                if(setup.get(e.getPlayer()).getStringList("Messages.Checkpoint") == null) return;
                e.setLine(0, setup.get(e.getPlayer()).getStringList("Messages.Checkpoint").get(0).replace("&", "§"));
                e.setLine(1, setup.get(e.getPlayer()).getStringList("Messages.Checkpoint").get(1).replace("&", "§"));
                e.setLine(2, setup.get(e.getPlayer()).getStringList("Messages.Checkpoint").get(2).replace("&", "§"));
                e.setLine(3, setup.get(e.getPlayer()).getStringList("Messages.Checkpoint").get(3).replace("&", "§"));

            }
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        Map<Player, YamlConfiguration> setup = EventoCommand.getSetupList();

        if(!setup.containsKey(e.getPlayer())) return;
        if(!setup.get(e.getPlayer()).isSet("Locations.Pos1")) return;

        if(e.getItem() == null || (e.getItem().getType() != Material.STONE_AXE && e.getItem().getType() != Material.STONE_HOE)) return;
        if(e.getItem().getItemMeta().getDisplayName() == null) return;

        if(e.getItem().getItemMeta().getDisplayName().equals("§6Machado de Posições")) {

            YamlConfiguration settings = setup.get(e.getPlayer());

            // Se for um click com o machado de posições, então obtenha a posição dos blocos e detecte o click.
            e.setCancelled(true);

            if(e.getAction() == Action.LEFT_CLICK_BLOCK) {

                settings.set("Locations.Pos1.world", e.getPlayer().getWorld().getName());
                settings.set("Locations.Pos1.x", e.getClickedBlock().getX());
                settings.set("Locations.Pos1.y", e.getClickedBlock().getY());
                settings.set("Locations.Pos1.z", e.getClickedBlock().getZ());

                try {
                    EventoConfigFile.save(settings);
                    setup.replace(e.getPlayer(), settings);
                } catch (IOException ex) {
                    e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "§").replace("@name", settings.getString("Evento.Title")));
                    ex.printStackTrace();
                }

                e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "§").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos1 "));

            }

            if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                settings.set("Locations.Pos2.world", e.getPlayer().getWorld().getName());
                settings.set("Locations.Pos2.x", e.getClickedBlock().getX());
                settings.set("Locations.Pos2.y", e.getClickedBlock().getY());
                settings.set("Locations.Pos2.z", e.getClickedBlock().getZ());

                try {
                    EventoConfigFile.save(settings);
                    setup.replace(e.getPlayer(), settings);
                } catch (IOException ex) {
                    e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "§").replace("@name", settings.getString("Evento.Title")));
                    ex.printStackTrace();
                }

                e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "§").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos2 "));

            }
        }

        if(e.getItem().getItemMeta().getDisplayName().equals("§6Enxada de Posições")) {

            YamlConfiguration settings = setup.get(e.getPlayer());

            // Se for um click com o machado de posições, então obtenha a posição dos blocos e detecte o click.
            e.setCancelled(true);

            if(e.getAction() == Action.LEFT_CLICK_BLOCK) {

                settings.set("Locations.Pos3.world", e.getPlayer().getWorld().getName());
                settings.set("Locations.Pos3.x", e.getClickedBlock().getX());
                settings.set("Locations.Pos3.y", e.getClickedBlock().getY());
                settings.set("Locations.Pos3.z", e.getClickedBlock().getZ());

                try {
                    EventoConfigFile.save(settings);
                    setup.replace(e.getPlayer(), settings);
                } catch (IOException ex) {
                    e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "§").replace("@name", settings.getString("Evento.Title")));
                    ex.printStackTrace();
                }

                e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "§").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos3 "));

            }

            if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                settings.set("Locations.Pos4.world", e.getPlayer().getWorld().getName());
                settings.set("Locations.Pos4.x", e.getClickedBlock().getX());
                settings.set("Locations.Pos4.y", e.getClickedBlock().getY());
                settings.set("Locations.Pos4.z", e.getClickedBlock().getZ());

                try {
                    EventoConfigFile.save(settings);
                    setup.replace(e.getPlayer(), settings);
                } catch (IOException ex) {
                    e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "§").replace("@name", settings.getString("Evento.Title")));
                    ex.printStackTrace();
                }

                e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "§").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos4 "));

            }
        }

    }

}
