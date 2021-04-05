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
import com.ars3ne.eventos.listeners.eventos.HunterListener;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

@SuppressWarnings("deprecation")
public class Hunter extends Evento {

    private final YamlConfiguration config;
    private final HunterListener listener = new HunterListener();

    private final Location blue, red;

    private final HashMap<Player, Integer> blue_team = new HashMap<>();
    private final HashMap<Player, Integer> red_team = new HashMap<>();
    private final List<Player> captured_players = new ArrayList<>();
    private int blue_points = 0;
    private int red_points = 0;
    private final List<ClanPlayer> clans = new ArrayList<>();

    private final int enable_pvp;
    private final int kill_points;
    private final int max_points;
    private final int capture_time;

    private final String blue_name;
    private final String red_name;
    private boolean pvp_enabled, team_selected = false;

    final Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
    final Team scoreboard_team_blue = board.registerNewTeam("blue_hunter");
    final Team scoreboard_team_red = board.registerNewTeam("red_hunter");
    final Team scoreboard_team_captured = board.registerNewTeam("captured_hunter");

    public Hunter(YamlConfiguration config) {
        super(config);
        this.config = config;
        this.blue_name = config.getString("Evento.Blue");
        this.red_name = config.getString("Evento.Red");
        this.enable_pvp = config.getInt("Evento.Enable PvP");
        this.kill_points = config.getInt("Evento.Points");
        this.max_points = config.getInt("Evento.Max points");
        this.capture_time = config.getInt("Evento.Capture time");

        World world = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Pos1.world"));
        this.blue = new Location(world, config.getDouble("Locations.Pos1.x"), config.getDouble("Locations.Pos1.y"), config.getDouble("Locations.Pos1.z"));
        this.red = new Location(world, config.getDouble("Locations.Pos2.x"), config.getDouble("Locations.Pos2.y"), config.getDouble("Locations.Pos2.z"));

        scoreboard_team_blue.setPrefix(ChatColor.BLUE.toString());
        scoreboard_team_red.setPrefix(ChatColor.RED.toString());
        scoreboard_team_captured.setPrefix(ChatColor.BLACK.toString());

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
            if(i % 2 == 0) blue_team.put(p, 0);
            else red_team.put(p, 0);
        }

        team_selected = true;

        List<String> team_st = config.getStringList("Messages.Team");

        ItemStack bow = new ItemStack(Material.BOW, 1);
        ItemMeta meta = bow.getItemMeta();
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        bow.setItemMeta(meta);

        // De itens aos jogadores.
        for(Player p: getPlayers()) {

            if(blue_team.containsKey(p)) {
                for(String s: team_st) {
                    p.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@team", "§9" + blue_name).replace("@time", String.valueOf(enable_pvp)));
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

                scoreboard_team_blue.addPlayer(p);
                p.teleport(blue);

            }

            if(red_team.containsKey(p)) {
                for(String s: team_st) {
                    p.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@team", "§c" + red_name).replace("@time", String.valueOf(enable_pvp)));
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

                scoreboard_team_red.addPlayer(p);

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
        }, enable_pvp * 20L);

    }

    public void win(String team) {

        if(!team_selected) return;

        List<String> winners = new ArrayList<>();

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinners();

        // Encerre o evento.
        this.stop();

        if(team.equals("blue")) {

            for (Player player : getPlayers()) {
                player.sendMessage(config.getString("Messages.Win").replace("&", "§").replace("@team", this.blue_name).replace("@points", String.valueOf(blue_points)));
            }
            for (Player player : getSpectators()) {
                player.sendMessage(config.getString("Messages.Win").replace("&", "§").replace("@team", this.blue_name).replace("@points", String.valueOf(blue_points)));
            }

            // Obtenha todos os jogadores restantes e os entregue as recompensas.
            for(Player p: blue_team.keySet()) {
                List<String> commands = this.config.getStringList("Rewards.Commands");
                for(String s : commands) {
                    executeConsoleCommand(p, s.replace("@winner", p.getName()));
                }

                // Adicione o nome á lista de vencedores.
                winners.add(p.getName());
            }

        }else {

            for (Player player : getPlayers()) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Win").replace("&", "§").replace("@team", this.red_name).replace("@points", String.valueOf(red_points)));
            }
            for (Player player : getSpectators()) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Win").replace("&", "§").replace("@team", this.red_name).replace("@points", String.valueOf(red_points)));
            }

            // Obtenha todos os jogadores restantes e os entregue as recompensas.
            for(Player p: red_team.keySet()) {
                List<String> commands = this.config.getStringList("Rewards.Commands");
                for(String s : commands) {
                    executeConsoleCommand(p, s.replace("@winner", p.getName()));
                }

                // Adicione o nome á lista de vencedores.
                winners.add(p.getName());
            }

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

        if(blue_team.size() == 0) win("red");
        if(red_team.size() == 0) win("blue");

        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);

        scoreboard_team_blue.removePlayer(p);
        scoreboard_team_red.removePlayer(p);
        scoreboard_team_captured.removePlayer(p);

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);
        this.remove(p);
    }

    @Override
    public void stop() {

        // Remova a armadura dos jogadores.
        for(Player p: getPlayers()) {
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
            p.getInventory().setBoots(null);
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.SLOW);
            scoreboard_team_blue.removePlayer(p);
            scoreboard_team_red.removePlayer(p);
            scoreboard_team_captured.removePlayer(p);
        }

        // Remova os times.
        scoreboard_team_blue.unregister();
        scoreboard_team_red.unregister();
        scoreboard_team_captured.unregister();

        // Desative o friendly-fire dos jogadores.
        List<ClanPlayer> remove_clan = new ArrayList<>();
        for (ClanPlayer p : clans) {
            p.setFriendlyFire(false);
            remove_clan.add(p);
        }

        for(ClanPlayer p: remove_clan) {
            clans.remove(p);
        }
        remove_clan.clear();

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    public void eliminate(Player captured, Player shooter) {

        // Mude a armadura do capturado e o paralize.
        captured_players.add(captured);
        captured.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 500000, 500)));
        captured.addPotionEffect((new PotionEffect(PotionEffectType.BLINDNESS, 500000, 500)));

        ItemStack helmet = new ItemStack(Material.JACK_O_LANTERN, 1);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        LeatherArmorMeta ch = (LeatherArmorMeta) chestplate.getItemMeta();
        ch.setColor(Color.BLACK);
        chestplate.setItemMeta(ch);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        LeatherArmorMeta lg = (LeatherArmorMeta) leggings.getItemMeta();
        lg.setColor(Color.BLACK);
        leggings.setItemMeta(ch);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta bo = (LeatherArmorMeta) boots.getItemMeta();
        bo.setColor(Color.BLACK);
        boots.setItemMeta(ch);
        captured.getInventory().setHelmet(helmet);
        captured.getInventory().setChestplate(chestplate);
        captured.getInventory().setLeggings(leggings);
        captured.getInventory().setBoots(boots);

        scoreboard_team_blue.removePlayer(captured);
        scoreboard_team_red.removePlayer(captured);
        scoreboard_team_captured.addPlayer(captured);

        // Adicione um ponto para o shooter.
        if(blue_team.containsKey(shooter)) {
            blue_team.put(shooter, blue_team.get(shooter) + this.kill_points);
            blue_points += this.kill_points;
        }

        if(red_team.containsKey(shooter)) {
            red_team.put(shooter, red_team.get(shooter) + this.kill_points);
            red_points += this.kill_points;
        }

        // Mande a mensagem no chat.

        List<String> eliminated_st = config.getStringList("Messages.Eliminated");
        List<String> capturated_st = config.getStringList("Messages.Capturated");

        if(blue_team.containsKey(captured)) {
            for (Player player : getPlayers()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§9" + captured.getName()).replace("@blueteam", "§9" + blue_points).replace("@redteam", "§c" + red_points));
                }
            }
            for (Player player : getSpectators()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§9" + captured.getName()).replace("@blueteam", "§9" + blue_points).replace("@redteam", "§c" + red_points));
                }
            }
        }

        if(red_team.containsKey(captured)) {
            for (Player player : getPlayers()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§c" + captured.getName()).replace("@blueteam", "§9" + blue_points).replace("@redteam", "§c" + red_points));
                }
            }
            for (Player player : getSpectators()) {
                for(String s: eliminated_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@player", "§c" + captured.getName()).replace("@blueteam", "§9" + blue_points).replace("@redteam", "§c" + red_points));
                }
            }
        }

        for(String s: capturated_st) {
            captured.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@blueteam", "§9" + blue_points).replace("@redteam", "§c" + red_points).replace("@time", String.valueOf(this.capture_time)));
        }

        // Verifique se algum time obteve os pontos necessários para a vitória.
        if(this.max_points != 0) {
            if(team_selected && blue_points >= this.max_points) {
                win("blue");
            }

            if(team_selected && red_points >= this.max_points) {
                win("red");
            }
        }

        // Depois de alguns segundos, teleporte o capturado para o inicio.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            if(!isHappening()) return;

            captured_players.remove(captured);
            scoreboard_team_captured.removePlayer(captured);

            if(blue_team.containsKey(captured)) {
                ItemStack helmet2 = new ItemStack(Material.LEATHER_HELMET, 1);
                LeatherArmorMeta bl = (LeatherArmorMeta) helmet2.getItemMeta();
                bl.setColor(Color.BLUE);
                helmet2.setItemMeta(bl);
                ItemStack chestplate2 = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
                LeatherArmorMeta ch2 = (LeatherArmorMeta) chestplate2.getItemMeta();
                ch2.setColor(Color.BLUE);
                chestplate2.setItemMeta(ch2);
                ItemStack leggings2 = new ItemStack(Material.LEATHER_LEGGINGS, 1);
                LeatherArmorMeta lg2 = (LeatherArmorMeta) leggings2.getItemMeta();
                lg2.setColor(Color.BLUE);
                leggings2.setItemMeta(ch2);
                ItemStack boots2 = new ItemStack(Material.LEATHER_BOOTS, 1);
                LeatherArmorMeta bo2 = (LeatherArmorMeta) boots2.getItemMeta();
                bo2.setColor(Color.BLUE);
                boots.setItemMeta(ch2);
                captured.getInventory().setHelmet(helmet2);
                captured.getInventory().setChestplate(chestplate2);
                captured.getInventory().setLeggings(leggings2);
                captured.getInventory().setBoots(boots2);

                captured.removePotionEffect(PotionEffectType.BLINDNESS);
                captured.removePotionEffect(PotionEffectType.SLOW);

                scoreboard_team_blue.addPlayer(captured);
                captured.teleport(blue);

            }

            if(red_team.containsKey(captured)) {
                ItemStack helmet2 = new ItemStack(Material.LEATHER_HELMET, 1);
                LeatherArmorMeta bl = (LeatherArmorMeta) helmet2.getItemMeta();
                bl.setColor(Color.RED);
                helmet2.setItemMeta(bl);
                ItemStack chestplate2 = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
                LeatherArmorMeta ch2 = (LeatherArmorMeta) chestplate2.getItemMeta();
                ch2.setColor(Color.RED);
                chestplate2.setItemMeta(ch2);
                ItemStack leggings2 = new ItemStack(Material.LEATHER_LEGGINGS, 1);
                LeatherArmorMeta lg2 = (LeatherArmorMeta) leggings2.getItemMeta();
                lg2.setColor(Color.RED);
                leggings2.setItemMeta(ch2);
                ItemStack boots2 = new ItemStack(Material.LEATHER_BOOTS, 1);
                LeatherArmorMeta bo2 = (LeatherArmorMeta) boots2.getItemMeta();
                bo2.setColor(Color.RED);
                boots.setItemMeta(ch2);
                captured.getInventory().setHelmet(helmet2);
                captured.getInventory().setChestplate(chestplate2);
                captured.getInventory().setLeggings(leggings2);
                captured.getInventory().setBoots(boots2);

                captured.removePotionEffect(PotionEffectType.BLINDNESS);
                captured.removePotionEffect(PotionEffectType.SLOW);

                scoreboard_team_red.addPlayer(captured);
                captured.teleport(red);
            }



    }, capture_time * 20L);

    }

    public HashMap<Player, Integer> getBlueTeam() {
        return this.blue_team;
    }
    public int getBluePoints() { return this.blue_points; }

    public HashMap<Player, Integer> getRedTeam() {
        return this.red_team;
    }

    public int getRedPoints() { return this.red_points; }

    public List<Player> getCaptured() { return this.captured_players; }

    public boolean isPvPEnabled() { return this.pvp_enabled; }

}