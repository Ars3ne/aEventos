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

package com.ars3ne.eventos.eventos;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.Evento;
import com.ars3ne.eventos.api.events.PlayerLoseEvent;
import com.ars3ne.eventos.listeners.eventos.FightListener;
import com.cryptomorin.xseries.XItemStack;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Fight extends Evento {

    private final YamlConfiguration config;
    private final FightListener listener = new FightListener();
    final Random random = new Random();

    private Player fighter1, fighter2;
    private final List<ClanPlayer> clans = new ArrayList<>();
    private final int interval;
    private final int max_time;
    private int task;
    private boolean fight_happening = false;
    private final Location entrance;
    private final Location fight1;
    private final Location fight2;

    public Fight(YamlConfiguration config) {
        super(config);

        this.config = config;
        this.interval = config.getInt("Evento.Time");
        this.max_time = config.getInt("Evento.Fight time");

        // Defina as localizações.
        World world = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Pos1.world"));
        entrance = new Location(world, config.getDouble("Locations.Entrance.x"), config.getDouble("Locations.Entrance.y"), config.getDouble("Locations.Entrance.z"), config.getLong("Locations.Entrance.Yaw"), config.getLong("Locations.Entrance.Pitch"));
        fight1 = new Location(world, config.getDouble("Locations.Pos1.x"), config.getDouble("Locations.Pos1.y"), config.getDouble("Locations.Pos1.z"));
        fight2 = new Location(world, config.getDouble("Locations.Pos2.x"), config.getDouble("Locations.Pos2.y"), config.getDouble("Locations.Pos2.z"));

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Se o servidor tiver SimpleClans, então ative o friendly fire.
        if(aEventos.getInstance().getSimpleClans() != null) {
            for(Player p: getPlayers()) {
                if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) != null) {
                    clans.add(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
                    aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(true);
                }
            }
        }

        // Inicie o evento.
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(aEventos.getInstance(), () -> {
            if(!isHappening()) Bukkit.getScheduler().cancelTask(task);
            if(fight_happening) return;
            fight();
        }, 20L, 20L);


    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title")));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = config.getStringList("Rewards.Commands");
        for(String s : commands) {
            executeConsoleCommand(p, s.replace("@winner", p.getName()));
        }

    }

    @Override
    public void stop() {

        // Remova a armadura dos jogadores.
        for(Player p: getPlayers()) {
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
            p.getInventory().setBoots(null);
            p.getInventory().clear();
        }

        // Desative o friendly-fire dos jogadores.
        for(ClanPlayer p: clans) {
            p.setFriendlyFire(false);
            clans.remove(p);
        }

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    @Override
    public void leave(Player p) {

        if(getPlayers().contains(p)) {
            for (Player player : getPlayers()) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName()));
            }

            for (Player player : getSpectators()) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName()));
            }

            // Desative o friendly-fire do jogador.
            if(aEventos.getInstance().getSimpleClans() != null) {
                if(clans.contains(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p))) {
                    clans.remove(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
                    aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(false);
                }
            }

            PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
            Bukkit.getPluginManager().callEvent(lose);

            // Se o jogador que saiu foi um dos lutadores, então o defina como perdedor.
            if(fighter1 == p || fighter2 == p) {
                setFightLoser(p);
            }else {
                this.remove(p);
            }

        }


    }

    private void fight() {

        if(!isHappening()) return;
        this.fight_happening = true;

        // Aguarde o tempo na config para iniciar a luta.
        List<String> next_st = config.getStringList("Messages.Next fight");
        for (Player player : getPlayers()) {
            for(String s : next_st) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(interval)).replace("@name", config.getString("Evento.Title")));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : next_st) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(interval)).replace("@name", config.getString("Evento.Title")));
            }
        }

        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            if(!isHappening()) return;

            // Obtenha dois jogadores aleatórios para iniciar o duelo.
            fighter1 = getPlayers().get(random.nextInt(getPlayers().size()));
            fighter2 = getPlayers().get(random.nextInt(getPlayers().size()));

            Player current_fighter1 = fighter1;
            Player current_fighter2 = fighter2;

            // Se o fighter2 for o mesmo que o fighter 1, pegue outro jogador.
            while(fighter2 == fighter1) {
                fighter2 = getPlayers().get(random.nextInt(getPlayers().size()));
            }

            // Mande a mensagem do início da luta.
            List<String> fight_st = config.getStringList("Messages.Fight");
            for (Player player : getPlayers()) {
                for(String s : fight_st) {
                    player.sendMessage(s.replace("&", "§").replace("@player1", fighter1.getName()).replace("@player2", fighter2.getName()).replace("@name", config.getString("Evento.Title")));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : fight_st) {
                    player.sendMessage(s.replace("&", "§").replace("@player1", fighter1.getName()).replace("@player2", fighter2.getName()).replace("@name", config.getString("Evento.Title")));
                }
            }

            // Se for a última luta, de os Items especiais.
            if(getPlayers().size() == 2) {

                if(config.getConfigurationSection("Itens.Last fight.Inventory") != null) {
                    for(String item: config.getConfigurationSection("Itens.Last fight.Inventory").getKeys(false)) {
                        fighter1.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Inventory." + item)));
                        fighter2.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Inventory." + item)));
                    }
                }

                if(config.getConfigurationSection("Itens.Last fight.Armor.Helmet") != null) {
                    fighter1.getInventory().setHelmet(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Helmet")));
                    fighter2.getInventory().setHelmet(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Helmet")));

                }

                if(config.getConfigurationSection("Itens.Last fight.Armor.Chestplate") != null) {
                    fighter1.getInventory().setChestplate(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Chestplate")));
                    fighter2.getInventory().setChestplate(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Chestplate")));
                }

                if(config.getConfigurationSection("Itens.Last fight.Armor.Leggings") != null) {
                    fighter1.getInventory().setLeggings(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Leggings")));
                    fighter2.getInventory().setLeggings(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Leggings")));
                }

                if(config.getConfigurationSection("Itens.Last fight.Armor.Boots") != null) {
                    fighter1.getInventory().setBoots(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Boots")));
                    fighter2.getInventory().setBoots(XItemStack.deserialize(config.getConfigurationSection("Itens.Last fight.Armor.Boots")));
                }

            }else {

                if(config.getConfigurationSection("Itens.Normal.Inventory") != null) {
                    for(String item: config.getConfigurationSection("Itens.Normal.Inventory").getKeys(false)) {
                        fighter1.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Inventory." + item)));
                        fighter2.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Inventory." + item)));
                    }
                }

                if(config.getConfigurationSection("Itens.Normal.Armor.Helmet") != null) {
                    fighter1.getInventory().setHelmet(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Helmet")));
                    fighter2.getInventory().setHelmet(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Helmet")));

                }

                if(config.getConfigurationSection("Itens.Normal.Armor.Chestplate") != null) {
                    fighter1.getInventory().setChestplate(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Chestplate")));
                    fighter2.getInventory().setChestplate(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Chestplate")));
                }

                if(config.getConfigurationSection("Itens.Normal.Armor.Leggings") != null) {
                    fighter1.getInventory().setLeggings(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Leggings")));
                    fighter2.getInventory().setLeggings(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Leggings")));
                }

                if(config.getConfigurationSection("Itens.Normal.Armor.Boots") != null) {
                    fighter1.getInventory().setBoots(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Boots")));
                    fighter2.getInventory().setBoots(XItemStack.deserialize(config.getConfigurationSection("Itens.Normal.Armor.Boots")));
                }
                
            }

            // Teleporte os lutadores.
            fighter1.setHealth(fighter1.getMaxHealth());
            fighter1.setFoodLevel(20);
            fighter1.teleport(fight1, PlayerTeleportEvent.TeleportCause.PLUGIN);
            fighter2.setHealth(fighter2.getMaxHealth());
            fighter2.setFoodLevel(20);
            fighter2.teleport(fight2, PlayerTeleportEvent.TeleportCause.PLUGIN);

            // Se a luta ainda estiver aconteçendo depois do tempo limite, a defina como um empate.
            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

                if(!isHappening()) return;
                if(!getPlayers().contains(current_fighter1) || !getPlayers().contains(current_fighter2)) return;
                if(fighter1 != current_fighter1 || fighter2 != current_fighter2) return;

                setFightLoser(null);

            }, max_time * 20L);

        }, interval * 20L);

    }

    public void setFightLoser(Player p) {

        // Se o player for null, significa que foi um empate.
        if(p == null) {

            // Mande a mensagem e teleporte os lutadores para a entrada.
            List<String> nowinner_st = config.getStringList("Messages.Fight no winner");
            for (Player player : getPlayers()) {
                for(String s : nowinner_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : nowinner_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }

            // Limpe o inventário de ambos.
            fighter1.getInventory().clear();
            fighter1.getInventory().setHelmet(null);
            fighter1.getInventory().setChestplate(null);
            fighter1.getInventory().setLeggings(null);
            fighter1.getInventory().setBoots(null);

            fighter2.getInventory().clear();
            fighter2.getInventory().setHelmet(null);
            fighter2.getInventory().setChestplate(null);
            fighter2.getInventory().setLeggings(null);
            fighter2.getInventory().setBoots(null);

            // Teleporte ambos para a entrada.
            fighter1.setHealth(fighter1.getMaxHealth());
            fighter1.setFoodLevel(20);
            fighter1.teleport(entrance, PlayerTeleportEvent.TeleportCause.PLUGIN);
            fighter2.setHealth(fighter2.getMaxHealth());
            fighter2.setFoodLevel(20);
            fighter2.teleport(entrance, PlayerTeleportEvent.TeleportCause.PLUGIN);

        }else {

            if(p != fighter1 && p != fighter2) return;

            // Limpe o inventário de ambos.
            fighter1.getInventory().clear();
            fighter1.getInventory().setHelmet(null);
            fighter1.getInventory().setChestplate(null);
            fighter1.getInventory().setLeggings(null);
            fighter1.getInventory().setBoots(null);
            fighter2.getInventory().clear();
            fighter2.getInventory().setHelmet(null);
            fighter2.getInventory().setChestplate(null);
            fighter2.getInventory().setLeggings(null);
            fighter2.getInventory().setBoots(null);

            // Mande a mensagem de vitória e teleporte o vencedor para a entrada.
            List<String> winner_st = config.getStringList("Messages.Fight winner");
            if(p == fighter1) {
                for (Player player : getPlayers()) {
                    for(String s : winner_st) {
                        player.sendMessage(s.replace("&", "§").replace("@winner", fighter2.getName()).replace("@name", config.getString("Evento.Title")));
                    }
                }
                for (Player player : getSpectators()) {
                    for(String s : winner_st) {
                        player.sendMessage(s.replace("&", "§").replace("@winner", fighter2.getName()).replace("@name", config.getString("Evento.Title")));
                    }
                }

                fighter2.setHealth(fighter2.getMaxHealth());
                fighter2.setFoodLevel(20);
                fighter2.teleport(entrance, PlayerTeleportEvent.TeleportCause.PLUGIN);

            }else {
                for (Player player : getPlayers()) {
                    for(String s : winner_st) {
                        player.sendMessage(s.replace("&", "§").replace("@winner", fighter1.getName()).replace("@name", config.getString("Evento.Title")));
                    }
                }
                for (Player player : getSpectators()) {
                    for(String s : winner_st) {
                        player.sendMessage(s.replace("&", "§").replace("@winner", fighter1.getName()).replace("@name", config.getString("Evento.Title")));
                    }
                }

                fighter1.setHealth(fighter1.getMaxHealth());
                fighter1.setFoodLevel(20);
                fighter1.teleport(entrance, PlayerTeleportEvent.TeleportCause.PLUGIN);

            }

            p.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
            remove(p);
            PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
            Bukkit.getPluginManager().callEvent(lose);

        }

        this.fight_happening = false;

    }

    public Player getFighter1() {
        return this.fighter1;
    }

    public Player getFighter2() {
        return this.fighter2;
    }


}
