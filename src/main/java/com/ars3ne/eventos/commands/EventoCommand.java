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

package com.ars3ne.eventos.commands;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.hooks.BungeecordHook;
import com.ars3ne.eventos.manager.InventoryManager;
import com.ars3ne.eventos.manager.InventorySerializer;
import com.ars3ne.eventos.utils.EventoConfigFile;
import com.ars3ne.eventos.utils.NumberFormatter;
import com.ars3ne.eventos.utils.Utils;
import com.cryptomorin.xseries.XItemStack;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventoCommand implements CommandExecutor {

    public static final Map<Player, YamlConfiguration> setup = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(cmd.getName().equalsIgnoreCase("evento")) {

            if(args.length == 0) {

                // Se n??o est?? acontecendo nenhum evento, ent??o envie as mensagens com os comandos.
                if(aEventos.getEventoManager().getEvento() == null) {

                    // Se o GUI estiver ativado, ent??o abra o invent??rio principal.
                    if(aEventos.getInstance().getConfig().getBoolean("Enable GUI")) {

                        if(sender instanceof Player) {
                            InventoryManager.openMainInventory((Player)sender);
                        }

                        if(!aEventos.getInstance().getConfig().getBoolean("Show commands")) return true;
                    }

                    // Se tiver a permiss??o de admin, ent??o mande os comandos de admin.
                    if(sender.hasPermission("aeventos.admin")) {
                        List<String> broadcast_messages = aEventos.getInstance().getConfig().getStringList("Messages.DefaultAdmin");
                        for(String s : broadcast_messages) {
                            sender.sendMessage(IridiumColorAPI.process(s.replace("&", "??")));
                        }
                    }else {
                        List<String> broadcast_messages = aEventos.getInstance().getConfig().getStringList("Messages.Default");
                        for(String s : broadcast_messages) {
                            sender.sendMessage(IridiumColorAPI.process(s.replace("&", "??")));
                        }
                    }

                }else {
                    // Se tem um evento ocorrendo, ent??o entre nele.

                    // Se o executor n??o ?? um player, mande um erro.
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Console").replace("&", "??")));
                        return true;
                    }
                    Player p = (Player) sender;

                    // Se o evento est?? fechado, retorne um erro.
                    if(!aEventos.getEventoManager().getEvento().isOpen()) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Closed").replace("&", "??")));
                        return true;
                    }

                    // Se o jogador n??o tem a permiss??o para participar do evento, retorne um erro.
                    if(!p.hasPermission(aEventos.getEventoManager().getEvento().getPermission())) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not allowed").replace("&", "??")));
                        return true;
                    }

                    // Se o usu??rio j?? est?? no evento, retorne um erro.
                    if(aEventos.getEventoManager().getEvento().getPlayers().contains(p)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Already joined").replace("&", "??")));
                        return true;
                    }

                    // Se o usu??rio j?? est?? no modo espectador, retorne um erro.
                    if(aEventos.getEventoManager().getEvento().getSpectators().contains(p)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Already spectator").replace("&", "??")));
                        return true;
                    }


                    // Se o evento requer um invent??rio vazio, e o usu??rio possui itens no invent??rio, retorne um erro.
                    if(aEventos.getEventoManager().getEvento().requireEmptyInventory()) {

                        if(aEventos.getInstance().getConfig().getBoolean("Save inventory")) {
                            InventorySerializer.serialize(p, aEventos.getEventoManager().getEvento().getIdentifier());
                        }else {
                            if(Utils.isInventoryFull(p)) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Empty inventory").replace("&", "??")));
                                return true;
                            }
                        }

                    }

                    // Entre no evento.
                    aEventos.getEventoManager().getEvento().joinBungeecord(p);
                }
            }

            if(args.length >= 1) {

                if(args[0].equalsIgnoreCase("sair")) {

                    // Se o executor n??o ?? um player, mande um erro.
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Console").replace("&", "??")));
                        return true;
                    }
                    Player p = (Player) sender;

                    // Se n??o est?? acontecendo um evento, mande um erro.
                    if(aEventos.getEventoManager().getEvento() == null) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No event").replace("&", "??")));
                        return true;
                    }

                    // Se o usu??rio n??o est?? no evento, retorne um erro.
                    if(!aEventos.getEventoManager().getEvento().getPlayers().contains(p) && !aEventos.getEventoManager().getEvento().getSpectators().contains(p)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not joined").replace("&", "??")));
                        return true;
                    }

                    // Saia do evento
                    aEventos.getEventoManager().getEvento().leaveBungeecord(p);

                }

                else if(args[0].equalsIgnoreCase("assistir") || args[0].equalsIgnoreCase("camarote")) {

                    // Se o executor n??o ?? um player, mande um erro.
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Console").replace("&", "??")));
                        return true;
                    }
                    Player p = (Player) sender;

                    // Se n??o est?? acontecendo um evento, mande um erro.
                    if(aEventos.getEventoManager().getEvento() == null) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No event").replace("&", "??")));
                        return true;
                    }

                    // Se o jogador n??o tem a permiss??o para assistir no modo espectador, retorne um erro.
                    if(!p.hasPermission("aeventos.spectator")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    // Se o jogador n??o tem a permiss??o para participar do evento, retorne um erro.
                    if(!p.hasPermission(aEventos.getEventoManager().getEvento().getPermission())) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not allowed").replace("&", "??")));
                        return true;
                    }

                    // Se o modo espectador est?? desativado no evento, retorne um erro.
                    if(!aEventos.getEventoManager().getEvento().isSpectatorAllowed()) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No spectator").replace("&", "??")));
                        return true;
                    }

                    // Se o usu??rio j?? est?? no evento, retorne um erro.
                    if(aEventos.getEventoManager().getEvento().getPlayers().contains(p)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Already joined").replace("&", "??")));
                        return true;
                    }

                    // Se o usu??rio j?? est?? no modo espectador, retorne um erro.
                    if(aEventos.getEventoManager().getEvento().getSpectators().contains(p)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Already spectator").replace("&", "??")));
                        return true;
                    }

                    // Se o usu??rio n??o est?? com o invent??rio vazio, retorne um erro.
                    if(Utils.isInventoryFull(p)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Empty inventory").replace("&", "??")));
                        return true;
                    }

                    // Entre no modo espectador.
                    aEventos.getEventoManager().getEvento().spectateBungeecord(p);

                }

                else if(args[0].equalsIgnoreCase("parar") || args[0].equalsIgnoreCase("cancelar")) {

                    // Se o usu??rio n??o tem a permiss??o, mande um erro.
                    if(!sender.hasPermission("aeventos.admin")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    // Se n??o est?? acontecendo um evento, mande um erro.
                    if(aEventos.getEventoManager().getEvento() == null && aEventos.getEventoChatManager().getEvento() == null) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No event").replace("&", "??")));
                        return true;
                    }

                    // Mande a mensagem de cancelamento e pare o evento.
                    YamlConfiguration config;

                    if(aEventos.getEventoManager().getEvento() != null) {
                        config = aEventos.getEventoManager().getEvento().getConfig();
                        aEventos.getEventoManager().getEvento().stop();

                        if(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Enabled") && config.getString("Locations.Server") != null){
                            BungeecordHook.stopEvento("cancelled");
                        }

                    }else {
                        config = aEventos.getEventoChatManager().getEvento().getConfig();
                        aEventos.getEventoChatManager().getEvento().stop();
                    }

                    List<String> broadcast_messages = config.getStringList("Messages.Cancelled");
                    for(String s : broadcast_messages) {
                        aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "??").replace("@name", config.getString("Evento.Title"))));
                    }

                    return true;

                }

                else if(args[0].equalsIgnoreCase("reload")) {

                    // Se o usu??rio n??o tem a permiss??o, mande um erro.
                    if(!sender.hasPermission("aeventos.admin")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    aEventos.getInstance().reloadConfig();
                    aEventos.updateTags();
                    aEventos.getCacheManager().updateCache();
                    InventoryManager.reload();

                    sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Reloaded").replace("&", "??")));
                    return true;

                }
                // Dois argumentos.

                else if(args[0].equalsIgnoreCase("iniciar")) {

                    // Se o usu??rio n??o tem a permiss??o, mande um erro.
                    if(!sender.hasPermission("aeventos.admin")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    // Se existe apenas um argumento, mande um erro.
                    if(args.length == 1) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Missing arguments").replace("&", "??").replace("@args", "iniciar <evento>")));
                        return true;
                    }

                    // Se j?? est?? acontecendo um evento, mande um erro.
                    if(aEventos.getEventoManager().getEvento() != null || aEventos.getEventoChatManager().getEvento() != null) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Already happening").replace("&", "??")));
                        return true;
                    }

                    // Tente obter o evento para ser iniciado. Se for inv??lido, mande um erro.
                    if(!EventoConfigFile.exists(args[1].toLowerCase())) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Invalid event").replace("&", "??")));
                        return true;
                    }

                    // Inicie o evento.
                    YamlConfiguration config = EventoConfigFile.get(args[1].toLowerCase());

                    // Se existem tr??s argumentos, ent??o inicie o evento com o valor especificado.
                    double reward = -1;
                    if(args.length == 3) {
                        reward = NumberFormatter.parseLetter(args[2]);
                    }

                    if(EventoType.isEventoChat(EventoType.getEventoType(config.getString("Evento.Type")))) {

                        boolean started = aEventos.getEventoChatManager().startEvento(EventoType.getEventoType(config.getString("Evento.Type")), config, reward);
                        if(!started) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Missing dependency").replace("&", "??").replace("@dependency", "Vault")));
                        }

                    }else {
                        boolean started = aEventos.getEventoManager().startEvento(EventoType.getEventoType(config.getString("Evento.Type")), config, reward);
                        if(!started) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not configurated").replace("&", "??").replace("@name", args[1].toLowerCase())));
                        }
                    }

                    return true;

                }else if(args[0].equalsIgnoreCase("forcestart")) {

                    // Se o usu??rio n??o tem a permiss??o, mande um erro.
                    if (!sender.hasPermission("aeventos.admin")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    // Se n??o est?? acontecendo um evento, mande um erro.
                    if(aEventos.getEventoManager().getEvento() == null) {

                        // Se n??o estiver acontecendo um evento em chat no momento, retorne.
                        if(aEventos.getEventoChatManager().getEvento() == null) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No event").replace("&", "??")));
                        }else {
                            // Inicie o evento em chat.
                            aEventos.getEventoChatManager().getEvento().forceStart();
                        }
                        return true;

                    }

                    // Se o evento est?? fechado, retorne um erro.
                    if(!aEventos.getEventoManager().getEvento().isOpen()) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Closed").replace("&", "??")));
                        return true;
                    }


                    // Force a inicializa????o do evento.
                    aEventos.getEventoManager().getEvento().forceStart();

                    return true;

                }else if(args[0].equalsIgnoreCase("ajuda") || args[0].equalsIgnoreCase("help")) {

                    // Se tiver a permiss??o de admin, ent??o mande os comandos de admin.
                    if(sender.hasPermission("aeventos.admin")) {
                        List<String> broadcast_messages = aEventos.getInstance().getConfig().getStringList("Messages.DefaultAdmin");
                        for(String s : broadcast_messages) {
                            sender.sendMessage(IridiumColorAPI.process(s.replace("&", "??")));
                        }
                    }else {
                        List<String> broadcast_messages = aEventos.getInstance().getConfig().getStringList("Messages.Default");
                        for(String s : broadcast_messages) {
                            sender.sendMessage(IridiumColorAPI.process(s.replace("&", "??")));
                        }
                    }

                    return true;

                }else if(args[0].equalsIgnoreCase("kick")) {

                    // Se o usu??rio n??o tem a permiss??o, mande um erro.
                    if(!sender.hasPermission("aeventos.admin")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    // Se existe apenas um argumento, mande um erro.
                    if(args.length == 1) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Missing arguments").replace("&", "??").replace("@args", "kick <jogador>")));
                        return true;
                    }

                    // Se o jogador alvo n??o est?? online, retorne.
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if(target == null) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Offline").replace("&", "??")));
                        return true;
                    }

                    // Se n??o est?? acontecendo um evento, mande um erro.
                    if(aEventos.getEventoManager().getEvento() == null) {

                        // Se n??o estiver acontecendo um evento em chat no momento, retorne.
                        if(aEventos.getEventoChatManager().getEvento() == null) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No event").replace("&", "??")));
                        }else {

                            // Se o jogador n??o est?? no evento, retorne.
                            if (!aEventos.getEventoChatManager().getEvento().getPlayers().contains(target)) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Player not joined").replace("&", "??")));
                                return true;
                            }

                            // Expulse o jogador do evento.
                            aEventos.getEventoChatManager().getEvento().leave(target);
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Player kicked").replace("&", "??")));
                            target.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Kicked").replace("&", "??")));
                            return true;

                        }

                    }else {

                        // Se o jogador n??o est?? no evento, retorne.
                        if (!aEventos.getEventoManager().getEvento().getPlayers().contains(target)) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Player not joined").replace("&", "??")));
                            return true;
                        }

                        // Expulse o jogador do evento.
                        aEventos.getEventoManager().getEvento().leave(target);
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Player kicked").replace("&", "??")));
                        target.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Kicked").replace("&", "??")));

                    }
                    return true;

                }else if(args[0].equalsIgnoreCase("criarconfig")) {

                    // Se o usu??rio n??o tem a permiss??o, mande um erro.
                    if (!sender.hasPermission("aeventos.admin")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    // Se existe apenas um argumento, mande um erro.
                    if (args.length == 1) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Missing arguments").replace("&", "??").replace("@args", "criarconfig <evento>")));
                        return true;
                    }

                    // Se a resource n??o existir, mande um erro.
                    if (aEventos.getInstance().getResource("eventos/" + args[1].toLowerCase() + ".yml") == null) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Invalid event").replace("&", "??")));
                        return true;
                    }

                    // Se o arquivo de configura????o j?? existir, mande um erro.
                    if (EventoConfigFile.exists(args[1].toLowerCase())) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Configuration already exists").replace("&", "??")));
                        return true;
                    }

                    // Crie o arquivo de configura????o e atualize as tags.
                    EventoConfigFile.create(args[1].toLowerCase());
                    aEventos.updateTags();

                    sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Configuration created").replace("&", "??").replace("@file", args[1].toLowerCase() + ".yml")));
                    return true;

                }else if(args[0].equalsIgnoreCase("setup")) {

                    // Se o executor n??o ?? um player, mande um erro.
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Console").replace("&", "??")));
                        return true;
                    }
                    Player p = (Player) sender;

                    // Se o usu??rio n??o tem a permiss??o, mande um erro.
                    if(!sender.hasPermission("aeventos.admin")) {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.No permission").replace("&", "??")));
                        return true;
                    }

                    // Se existe apenas um argumento, mande um erro.
                    if(args.length == 1) {

                        if(setup.containsKey(p)) {
                            List<String> broadcast_messages = aEventos.getInstance().getConfig().getStringList("Messages.Setup");
                            for(String s : broadcast_messages) {
                                sender.sendMessage(IridiumColorAPI.process(s.replace("&", "??").replace("@name", setup.get(p).getString("Evento.Title"))));
                            }
                        }else {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Missing arguments").replace("@args", "setup <evento>").replace("&", "??")));
                        }

                        return true;
                    }

                    // Se o usu??rio j?? est?? configurando um evento, ent??o verifique o argumento.
                    if(setup.containsKey(p)) {

                        YamlConfiguration settings = setup.get(p);

                        if(args[1].equalsIgnoreCase("entrada")) {
                            // Defina a entrada do evento para a localiza????o do usu??rio.
                            settings.set("Locations.Entrance.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Entrance.x", p.getLocation().getX());
                            settings.set("Locations.Entrance.y", p.getLocation().getY());
                            settings.set("Locations.Entrance.z", p.getLocation().getZ());
                            settings.set("Locations.Entrance.yaw", p.getLocation().getYaw());
                            settings.set("Locations.Entrance.pitch", p.getLocation().getPitch());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                            return true;
                        }

                        else if(args[1].equalsIgnoreCase("saida")) {
                            // Defina a entrada do evento para a localiza????o do usu??rio.
                            settings.set("Locations.Exit.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Exit.x", p.getLocation().getX());
                            settings.set("Locations.Exit.y", p.getLocation().getY());
                            settings.set("Locations.Exit.z", p.getLocation().getZ());
                            settings.set("Locations.Exit.yaw", p.getLocation().getYaw());
                            settings.set("Locations.Exit.pitch", p.getLocation().getPitch());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                            return true;
                        }

                        else if(args[1].equalsIgnoreCase("espera") || args[1].equalsIgnoreCase("fila") || args[1].equalsIgnoreCase("lobby")) {
                            // Defina a entrada do evento para a localiza????o do usu??rio.
                            settings.set("Locations.Lobby.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Lobby.x", p.getLocation().getX());
                            settings.set("Locations.Lobby.y", p.getLocation().getY());
                            settings.set("Locations.Lobby.z", p.getLocation().getZ());
                            settings.set("Locations.Lobby.yaw", p.getLocation().getYaw());
                            settings.set("Locations.Lobby.pitch", p.getLocation().getPitch());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                            return true;
                        }

                        else if(args[1].equalsIgnoreCase("camarote") || args[1].equalsIgnoreCase("assistir")) {
                            // Defina a entrada do evento para a localiza????o do usu??rio.
                            settings.set("Locations.Spectator.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Spectator.x", p.getLocation().getX());
                            settings.set("Locations.Spectator.y", p.getLocation().getY());
                            settings.set("Locations.Spectator.z", p.getLocation().getZ());
                            settings.set("Locations.Spectator.yaw", p.getLocation().getYaw());
                            settings.set("Locations.Spectator.pitch", p.getLocation().getPitch());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                            return true;
                        }

                        else if(args[1].equalsIgnoreCase("pos")) {

                            // Se o evento n??o possui as configura????es de pos, retorne um erro.
                            if(!setup.get(p).isSet("Locations.Pos1")) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not needed").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                                return true;
                            }

                            // De um machado para o jogador definir as posi????es.
                            ItemStack axe = new ItemStack(Material.STONE_AXE, 1);
                            ItemMeta meta = axe.getItemMeta();

                            meta.setDisplayName("??6Machado de Posi????es");
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add("??7* Clique esquerdo para definir a primeira posi????o.");
                            lore.add("??7* Clique direito para definir a segunda posi????o.");
                            meta.setLore(lore);

                            axe.setItemMeta(meta);
                            p.getInventory().addItem(axe);

                            if(setup.get(p).isSet("Locations.Pos3")) {
                                // De uma enxada para o jogador definir as posi????es.
                                ItemStack hoe = new ItemStack(Material.STONE_HOE, 1);
                                ItemMeta meta2 = hoe.getItemMeta();

                                meta2.setDisplayName("??6Enxada de Posi????es");
                                ArrayList<String> lore2 = new ArrayList<>();
                                lore2.add("??7* Clique esquerdo para definir a terceira posi????o.");
                                lore2.add("??7* Clique direito para definir a quarta posi????o.");
                                meta2.setLore(lore2);

                                hoe.setItemMeta(meta2);
                                p.getInventory().addItem(hoe);
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Give axe").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                            return true;

                        }

                        else if(args[1].equalsIgnoreCase("pos1")) {

                            // Se o evento n??o possui as configura????es de pos, retorne um erro.
                            if(!setup.get(p).isSet("Locations.Pos1")) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not needed").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                                return true;
                            }

                            settings.set("Locations.Pos1.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Pos1.x", p.getLocation().getX());
                            settings.set("Locations.Pos1.y", p.getLocation().getY());
                            settings.set("Locations.Pos1.z", p.getLocation().getZ());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos1 ")));
                            return true;

                        }

                        else if(args[1].equalsIgnoreCase("pos2")) {

                            // Se o evento n??o possui as configura????es de pos, retorne um erro.
                            if(!setup.get(p).isSet("Locations.Pos1")) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not needed").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                                return true;
                            }

                            settings.set("Locations.Pos2.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Pos2.x", p.getLocation().getX());
                            settings.set("Locations.Pos2.y", p.getLocation().getY());
                            settings.set("Locations.Pos2.z", p.getLocation().getZ());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos2 ")));
                            return true;

                        }

                        else if(args[1].equalsIgnoreCase("pos3")) {

                            // Se o evento n??o possui as configura????es de pos, retorne um erro.
                            if(!setup.get(p).isSet("Locations.Pos3")) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not needed").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                                return true;
                            }

                            settings.set("Locations.Pos3.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Pos3.x", p.getLocation().getX());
                            settings.set("Locations.Pos3.y", p.getLocation().getY());
                            settings.set("Locations.Pos3.z", p.getLocation().getZ());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos3 ")));
                            return true;

                        }

                        else if(args[1].equalsIgnoreCase("pos4")) {

                            // Se o evento n??o possui as configura????es de pos, retorne um erro.
                            if(!setup.get(p).isSet("Locations.Pos3")) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not needed").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                                return true;
                            }

                            settings.set("Locations.Pos4.world", p.getLocation().getWorld().getName());
                            settings.set("Locations.Pos4.x", p.getLocation().getX());
                            settings.set("Locations.Pos4.y", p.getLocation().getY());
                            settings.set("Locations.Pos4.z", p.getLocation().getZ());

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos4 ")));
                            return true;

                        }

                        else if(args[1].equalsIgnoreCase("kit") || args[1].equalsIgnoreCase("item") || args[1].equalsIgnoreCase("itens")) {

                            // Se o evento n??o possui itens, retorne.

                            if(!setup.get(p).isSet("Itens")) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not needed kit").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                                return true;
                            }

                            if(args.length == 2) {
                                if(setup.get(p).isSet("Itens.Normal")) {
                                    sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Multiple kits").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                                    return true;
                                }
                            }


                            if(args.length >= 3) {

                                if(setup.get(p).isSet("Itens.Normal")) {

                                    if(args[2].equalsIgnoreCase("normal")) {

                                        // O serializer n??o aceita uma section vazia por algum motivo, ent??o eu sou obrigado a definir algo tempor??rio apenas para n??o ficar vazia.
                                        settings.set("Itens.Normal.Armor.Helmet", "");
                                        settings.set("Itens.Normal.Armor.Helmet.a", "shaark");
                                        settings.set("Itens.Normal.Armor.Chestplate", "");
                                        settings.set("Itens.Normal.Armor.Chestplate.a", "shaark");
                                        settings.set("Itens.Normal.Armor.Leggings", "");
                                        settings.set("Itens.Normal.Armor.Leggings.a", "shaark");
                                        settings.set("Itens.Normal.Armor.Boots", "");
                                        settings.set("Itens.Normal.Armor.Boots.a", "shaark");

                                        if(p.getInventory().getHelmet() != null) XItemStack.serialize(p.getInventory().getHelmet(), settings.getConfigurationSection("Itens.Normal.Armor.Helmet"));
                                        if(p.getInventory().getChestplate() != null) XItemStack.serialize(p.getInventory().getChestplate(), settings.getConfigurationSection("Itens.Normal.Armor.Chestplate"));
                                        if(p.getInventory().getLeggings() != null) XItemStack.serialize(p.getInventory().getLeggings(), settings.getConfigurationSection("Itens.Normal.Armor.Leggings"));
                                        if(p.getInventory().getBoots() != null) XItemStack.serialize(p.getInventory().getBoots(), settings.getConfigurationSection("Itens.Normal.Armor.Boots"));

                                        settings.set("Itens.Normal.Armor.Helmet.a", null);
                                        settings.set("Itens.Normal.Armor.Chestplate.a", null);
                                        settings.set("Itens.Normal.Armor.Leggings.a", null);
                                        settings.set("Itens.Normal.Armor.Boots.a", null);

                                        settings.set("Itens.Normal.Armor.Inventory", "");

                                        for(int i = 0; i < 36; i++) {
                                            if(p.getInventory().getItem(i) == null) continue;
                                            settings.set("Itens.Normal.Inventory." + i + ".a", "shaark");
                                            XItemStack.serialize(p.getInventory().getItem(i), settings.getConfigurationSection("Itens.Normal.Inventory." + i));
                                            settings.set("Itens.Normal.Inventory." + i + ".a", null);
                                        }

                                    }

                                    if(args[2].equalsIgnoreCase("last") || args[2].equalsIgnoreCase("lastfight")) {

                                        // O serializer n??o aceita uma section vazia por algum motivo, ent??o eu sou obrigado a definir algo tempor??rio apenas para n??o ficar vazia.
                                        settings.set("Itens.Last fight.Armor.Helmet", "");
                                        settings.set("Itens.Last fight.Armor.Helmet.a", "shaark");
                                        settings.set("Itens.Last fight.Armor.Chestplate", "");
                                        settings.set("Itens.Last fight.Armor.Chestplate.a", "shaark");
                                        settings.set("Itens.Last fight.Armor.Leggings", "");
                                        settings.set("Itens.Last fight.Armor.Leggings.a", "shaark");
                                        settings.set("Itens.Last fight.Armor.Boots", "");
                                        settings.set("Itens.Last fight.Armor.Boots.a", "shaark");

                                        if(p.getInventory().getHelmet() != null) XItemStack.serialize(p.getInventory().getHelmet(), settings.getConfigurationSection("Itens.Last fight.Armor.Helmet"));
                                        if(p.getInventory().getChestplate() != null) XItemStack.serialize(p.getInventory().getChestplate(), settings.getConfigurationSection("Itens.Last fight.Armor.Chestplate"));
                                        if(p.getInventory().getLeggings() != null) XItemStack.serialize(p.getInventory().getLeggings(), settings.getConfigurationSection("Itens.Last fight.Armor.Leggings"));
                                        if(p.getInventory().getBoots() != null) XItemStack.serialize(p.getInventory().getBoots(), settings.getConfigurationSection("Itens.Last fight.Armor.Boots"));

                                        settings.set("Itens.Last fight.Armor.Helmet.a", null);
                                        settings.set("Itens.Last fight.Armor.Chestplate.a", null);
                                        settings.set("Itens.Last fight.Armor.Leggings.a", null);
                                        settings.set("Itens.Last fight.Armor.Boots.a", null);

                                        settings.set("Itens.Last fight.Armor.Inventory", "");

                                        for(int i = 0; i < 36; i++) {
                                            if(p.getInventory().getItem(i) == null) continue;
                                            settings.set("Itens.Last fight.Inventory." + i + ".a", "shaark");
                                            XItemStack.serialize(p.getInventory().getItem(i), settings.getConfigurationSection("Itens.Last fight.Inventory." + i));
                                            settings.set("Itens.Last fight.Inventory." + i + ".a", null);
                                        }

                                    }

                                }

                            }else {

                                // Se o evento n??o for de times, ent??o salve a armadura.
                                switch(EventoType.getEventoType(settings.getString("Evento.Type"))) {
                                    case PAINTBALL: case HUNTER: case NEXUS:
                                        break;
                                    default:
                                        // O serializer n??o aceita uma section vazia por algum motivo, ent??o eu sou obrigado a definir algo tempor??rio apenas para n??o ficar vazia.
                                        settings.set("Itens.Helmet", "");
                                        settings.set("Itens.Helmet.a", "shaark");
                                        settings.set("Itens.Chestplate", "");
                                        settings.set("Itens.Chestplate.a", "shaark");
                                        settings.set("Itens.Leggings", "");
                                        settings.set("Itens.Leggings.a", "shaark");
                                        settings.set("Itens.Boots", "");
                                        settings.set("Itens.Boots.a", "shaark");

                                        if(p.getInventory().getHelmet() != null) XItemStack.serialize(p.getInventory().getHelmet(), settings.getConfigurationSection("Itens.Helmet"));
                                        if(p.getInventory().getChestplate() != null) XItemStack.serialize(p.getInventory().getChestplate(), settings.getConfigurationSection("Itens.Chestplate"));
                                        if(p.getInventory().getLeggings() != null) XItemStack.serialize(p.getInventory().getLeggings(), settings.getConfigurationSection("Itens.Leggings"));
                                        if(p.getInventory().getBoots() != null) XItemStack.serialize(p.getInventory().getBoots(), settings.getConfigurationSection("Itens.Boots"));

                                        settings.set("Itens.Helmet.a", null);
                                        settings.set("Itens.Chestplate.a", null);
                                        settings.set("Itens.Leggings.a", null);
                                        settings.set("Itens.Boots.a", null);

                                        break;

                                }

                                settings.set("Itens.Inventory", "");

                                for(int i = 0; i < 36; i++) {
                                    if(p.getInventory().getItem(i) == null) continue;
                                    settings.set("Itens.Inventory." + i + ".a", "shaark");
                                    XItemStack.serialize(p.getInventory().getItem(i), settings.getConfigurationSection("Itens.Inventory." + i));
                                    settings.set("Itens.Inventory." + i + ".a", null);
                                }

                            }

                            try {
                                EventoConfigFile.save(settings);
                                setup.replace(p, settings);
                            } catch (IOException e) {
                                sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Error").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "")));
                                e.printStackTrace();
                            }

                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Saved kit").replace("&", "??").replace("@name", settings.getString("Evento.Title")).replace("@pos", "pos4 ")));
                            return true;
                            
                        }

                        else if(args[1].equalsIgnoreCase("sair")) {
                            // Remova o usu??rio da lista de setup.
                            setup.remove(p);
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Exit setup").replace("&", "??").replace("@name", settings.getString("Evento.Title"))));
                            return true;
                        }

                        else {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Unknown argument").replace("&", "??")));
                            return true;
                        }

                    }else {
                        // Se n??o, tente achar o evento e adicione o usu??rio ?? lista. Se for inv??lido, mande um erro.
                        if(!EventoConfigFile.exists(args[1].toLowerCase())) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Invalid event").replace("&", "??")));
                            return true;
                        }

                        // Obtenha a configura????o do evento, adicione o usu??rio a lista e mostre a mensagem com os comandos.
                        YamlConfiguration config = EventoConfigFile.get(args[1].toLowerCase());
                        setup.put(p, config);

                        List<String> broadcast_messages = aEventos.getInstance().getConfig().getStringList("Messages.Setup");
                        for(String s : broadcast_messages) {
                            sender.sendMessage(IridiumColorAPI.process(s.replace("&", "??").replace("@name", config.getString("Evento.Title"))));
                        }

                        return true;
                    }
                }

                else {

                    if(aEventos.getEventoChatManager().getEvento() != null) {

                        if (!(sender instanceof Player)) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Console").replace("&", "??")));
                            return true;
                        }

                        Player p = (Player) sender;

                        // Se o jogador n??o tem a permiss??o para participar do evento, retorne um erro.
                        if(!p.hasPermission(aEventos.getEventoChatManager().getEvento().getPermission())) {
                            sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not allowed").replace("&", "??")));
                            return true;
                        }

                        aEventos.getEventoChatManager().getEvento().parseCommand(p, args);

                    }else {
                        sender.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Unknown command").replace("&", "??")));
                    }
                    return true;
                }

            }
        }

        return false;
    }

    public static Map<Player, YamlConfiguration> getSetupList() {
        return setup;
    }

}
