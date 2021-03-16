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
import com.ars3ne.eventos.listeners.eventos.PaintballListener;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Paintball extends Evento {

    private final YamlConfiguration config;
    private final PaintballListener listener = new PaintballListener();

    private final Location blue, red;

    private final List<Player> blue_team = new ArrayList<>();
    private final List<Player> red_team = new ArrayList<>();
    private final List<ClanPlayer> clans = new ArrayList<>();

    private final int time;
    private final String blue_name;
    private final String red_name;
    private boolean pvp_enabled;

    public Paintball(YamlConfiguration config) {
        super(config);
        this.config = config;
        this.blue_name = config.getString("Evento.Blue");
        this.red_name = config.getString("Evento.Red");
        this.time = config.getInt("Evento.Time");
        this.pvp_enabled = false;

        World world = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Pos1.world"));
        this.blue = new Location(world, config.getDouble("Locations.Pos1.x"), config.getDouble("Locations.Pos1.y"), config.getDouble("Locations.Pos1.z"));
        this.red = new Location(world, config.getDouble("Locations.Pos2.x"), config.getDouble("Locations.Pos2.y"), config.getDouble("Locations.Pos2.z"));

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Adicione os jogadores á times aleatórios.
        Collections.shuffle(getPlayers());
        for(int i = 0; i < getPlayers().size(); i++) {
            Player p = getPlayers().get(i);
            if(i % 2 == 0) blue_team.add(p);
            else red_team.add(p);
        }

        List<String> team_st = config.getStringList("Messages.Team");

        ItemStack bow = new ItemStack(Material.BOW, 1);
        ItemMeta meta = bow.getItemMeta();
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        bow.setItemMeta(meta);

        // De itens aos jogadores.
        for(Player p: getPlayers()) {

            if(blue_team.contains(p)) {
                for(String s: team_st) {
                    p.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@team", "§9" + blue_name).replace("@time", String.valueOf(time)));
                }
                ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
                LeatherArmorMeta bl = (LeatherArmorMeta) helmet.getItemMeta();
                bl.setColor(Color.BLUE);
                helmet.setItemMeta(bl);
                ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
                LeatherArmorMeta ch = (LeatherArmorMeta) chestplate.getItemMeta();
                ch.setColor(Color.BLUE);
                chestplate.setItemMeta(ch);
                ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
                LeatherArmorMeta lg = (LeatherArmorMeta) leggings.getItemMeta();
                lg.setColor(Color.BLUE);
                leggings.setItemMeta(ch);
                ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
                LeatherArmorMeta bo = (LeatherArmorMeta) boots.getItemMeta();
                bo.setColor(Color.BLUE);
                boots.setItemMeta(ch);
                p.getInventory().setHelmet(helmet);
                p.getInventory().setChestplate(chestplate);
                p.getInventory().setLeggings(leggings);
                p.getInventory().setBoots(boots);

                p.teleport(blue);
            }

            if(red_team.contains(p)) {
                for(String s: team_st) {
                    p.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@team", "§c" + red_name).replace("@time", String.valueOf(time)));
                }
                ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
                LeatherArmorMeta bl = (LeatherArmorMeta) helmet.getItemMeta();
                bl.setColor(Color.RED);
                helmet.setItemMeta(bl);
                ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
                LeatherArmorMeta ch = (LeatherArmorMeta) chestplate.getItemMeta();
                ch.setColor(Color.RED);
                chestplate.setItemMeta(ch);
                ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
                LeatherArmorMeta lg = (LeatherArmorMeta) leggings.getItemMeta();
                lg.setColor(Color.RED);
                leggings.setItemMeta(ch);
                ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
                LeatherArmorMeta bo = (LeatherArmorMeta) boots.getItemMeta();
                bo.setColor(Color.RED);
                boots.setItemMeta(ch);
                p.getInventory().setHelmet(helmet);
                p.getInventory().setChestplate(chestplate);
                p.getInventory().setLeggings(leggings);
                p.getInventory().setBoots(boots);

                p.teleport(red);
            }

            p.getInventory().addItem(bow);
            p.getInventory().addItem(new ItemStack(Material.ARROW, 1));

        }

        // Se o servidor tiver SimpleClans, então ative o friendly fire.
        if(aEventos.getInstance().getSimpleClans() != null) {
            for(Player p: getPlayers()) {
                if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) != null) {
                    clans.add(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
                    aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(true);
                }
            }
        }

        // Depois do tempo especificado na config, ative o PvP.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {
            if(!isHappening()) return;
            this.pvp_enabled = true;
            // Mande a mensagem de que o PvP está ativado.
            List<String> enabled_st = config.getStringList("Messages.Enabled");
            for (Player player : getPlayers()) {
                for(String s : enabled_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }
            for (Player player : getSpectators()) {
                for(String s : enabled_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }
        }, time * 20L);

    }

    public void win() {

        List<String> winners = new ArrayList<>();

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinners();

        // Encerre o evento.
        this.stop();

        // Obtenha todos os jogadores restantes e os entregue as recompensas.
        for(Player p: getPlayers()) {
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
            p.getInventory().setBoots(null);
            // Execute os comandos de vitória
            List<String> commands = this.config.getStringList("Rewards.Commands");
            for(String s : commands) {
                aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), s.replace("@winner", p.getName()));
            }

            // Adicione o nome á lista de vencedores.
            winners.add(p.getName());
        }

        // Mande a mensagem de vitória para o servidor.
        List<String> broadcast_messages = this.config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", String.join(", ", winners)).replace("@name", config.getString("Evento.Title")));
        }

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
        }

        // Desative o friendly-fire do jogador.
        if(aEventos.getInstance().getSimpleClans() != null) {
            if(clans.contains(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p))) {
                clans.remove(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
                aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(false);
            }
        }

        eliminate(p);
        this.remove(p);
    }

    @Override
    public void stop() {

        // Desative o friendly-fire dos jogadores.
        for(ClanPlayer p: clans) {
            p.setFriendlyFire(false);
            clans.remove(p);
        }

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    public void eliminate(Player p) {

        List<String> eliminated_st = config.getStringList("Messages.Eliminated");

        if(blue_team.contains(p)) {
            blue_team.remove(p);
            for (Player player : getPlayers()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§9" + p.getName()).replace("@remeaning", "§9" + blue_team.size()).replace("@team", "§9" + blue_name));
                }
            }
            for (Player player : getSpectators()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§9" + p.getName()).replace("@remeaning", "§9" + blue_team.size()).replace("@team", "§9" + blue_name));
                }            }
        }

        if(red_team.contains(p)) {
            red_team.remove(p);
            for (Player player : getPlayers()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§c" + p.getName()).replace("@remeaning", "§c" + red_team.size()).replace("@team", "§c" + red_name));
                }
            }
            for (Player player : getSpectators()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§c" + p.getName()).replace("@remeaning", "§c" + red_team.size()).replace("@team", "§c" + red_name));
                }
            }
        }

        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);
        this.remove(p);

        if(blue_team.size() == 0 || red_team.size() == 0) {
            win();
        }

    }
    public List<Player> getBlueTeam() {
        return this.blue_team;
    }

    public List<Player> getRedTeam() {
        return this.red_team;
    }

    public boolean isPvPEnabled() { return this.pvp_enabled; }

}
