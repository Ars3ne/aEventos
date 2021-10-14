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
import com.ars3ne.eventos.listeners.eventos.NexusListener;
import com.cryptomorin.xseries.XItemStack;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayer;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import yclans.api.yClansAPI;
import yclans.model.Clan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Nexus extends Evento {

    private final YamlConfiguration config;
    private final NexusListener listener = new NexusListener();

    private final Location blue_spawn, red_spawn;
    private final EnderCrystal blue_nexus;
    private final EnderCrystal red_nexus;

    private final HashMap<Player, Integer> blue_team = new HashMap<>();
    private final HashMap<Player, Integer> red_team = new HashMap<>();
    private final List<Player> dead_players = new ArrayList<>();
    private final List<Player> invincible_players = new ArrayList<>();

    private yClansAPI yclans_api;

    private final int enable_pvp;
    private final int respawn_interval;
    private final int damage;
    private final int invincibility_time;
    private final String nexus_name;
    private int blue_nexus_health, red_nexus_health;

    private final String blue_name;
    private final String red_name;
    private boolean pvp_enabled, team_selected = false;

    private final ArrayList<ClanPlayer> simpleclans_clans = new ArrayList<>();
    private final HashMap<MPlayer, Faction> massivefactions_factions = new HashMap<>();
    private final HashMap<yclans.model.ClanPlayer, Clan> yclans_clans = new HashMap<>();

    public Nexus(YamlConfiguration config) {

        super(config);
        this.config = config;
        this.blue_name = config.getString("Evento.Blue");
        this.red_name = config.getString("Evento.Red");
        this.enable_pvp = config.getInt("Evento.Enable PvP");
        int health = config.getInt("Evento.Health");
        this.respawn_interval = config.getInt("Evento.Respawn time");
        this.damage = config.getInt("Evento.Damage");
        this.nexus_name = config.getString("Evento.Nexus name");
        this.invincibility_time = config.getInt("Evento.Invincibility");

        World world = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Pos1.world"));
        this.blue_spawn = new Location(world, config.getDouble("Locations.Pos1.x"), config.getDouble("Locations.Pos1.y"), config.getDouble("Locations.Pos1.z"));
        this.red_spawn = new Location(world, config.getDouble("Locations.Pos2.x"), config.getDouble("Locations.Pos2.y"), config.getDouble("Locations.Pos2.z"));

        Location blue_nexus_loc = new Location(world, config.getDouble("Locations.Pos3.x"), config.getDouble("Locations.Pos3.y"), config.getDouble("Locations.Pos3.z"));
        Location red_nexus_loc = new Location(world, config.getDouble("Locations.Pos4.x"), config.getDouble("Locations.Pos4.y"), config.getDouble("Locations.Pos4.z"));

        // Remova os Nexus já existentes, se eles ainda não foram removidos.
        for(Entity entity: world.getEntities()) {
            if(entity.getType() != EntityType.ENDER_CRYSTAL) continue;
            if(entity.hasMetadata("Nexus")) entity.remove();
        }

        // Invoque os nexus.
        blue_nexus_health = health;
        red_nexus_health = health;

        blue_nexus = (EnderCrystal) world.spawnEntity(blue_nexus_loc, EntityType.ENDER_CRYSTAL);
        blue_nexus.setCustomName(IridiumColorAPI.process(nexus_name.replace("&", "§").replace("@team_color", "§9").replace("@team_uppercase", blue_name.toUpperCase()).replace("@team", blue_name).replace("@health", String.valueOf(blue_nexus_health))));
        blue_nexus.setMetadata("Nexus", new FixedMetadataValue(aEventos.getInstance(), true));
        blue_nexus.setMetadata("Blue", new FixedMetadataValue(aEventos.getInstance(), true));
        blue_nexus.setCustomNameVisible(true);

        red_nexus = (EnderCrystal) world.spawnEntity(red_nexus_loc, EntityType.ENDER_CRYSTAL);
        red_nexus.setCustomName(IridiumColorAPI.process(nexus_name.replace("&", "§").replace("@team_color", "§c").replace("@team_uppercase", red_name.toUpperCase()).replace("@team", red_name).replace("@health", String.valueOf(red_nexus_health))));
        red_nexus.setMetadata("Nexus", new FixedMetadataValue(aEventos.getInstance(), true));
        red_nexus.setMetadata("Red", new FixedMetadataValue(aEventos.getInstance(), true));
        red_nexus.setCustomNameVisible(true);

        // Registre o listener do evento.
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans")) {
            yclans_api = yClansAPI.yclansapi;
        }

    }

    @Override
    public void start() {

        // Registre o listener do evento.
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

            for(String item: config.getConfigurationSection("Itens.Inventory").getKeys(false)) {
                p.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Inventory." + item)));
            }

            if(blue_team.containsKey(p)) {
                for(String s: team_st) {
                    p.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@team", "§9" + blue_name).replace("@time", String.valueOf(enable_pvp))));
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

                p.teleport(blue_spawn);

            }

            if(red_team.containsKey(p)) {
                for(String s: team_st) {
                    p.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@team", "§c" + red_name).replace("@time", String.valueOf(enable_pvp))));
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

                p.teleport(red_spawn);
            }

        }

        // Se o servidor tiver SimpleClans, então ative o friendly fire.
        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            for (Player p : getPlayers()) {
                if (aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) != null) {
                    simpleclans_clans.add(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
                    aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(true);
                }
            }
        }

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("massivefactions") && aEventos.getInstance().isHookedMassiveFactions()) {
            for (Player p : getPlayers()) {
                massivefactions_factions.put(MPlayer.get(p), MPlayer.get(p).getFaction());
                MPlayer.get(p).getFaction().setFlag(MFlag.ID_FRIENDLYFIRE, true);
            }
        }

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans") && aEventos.getInstance().isHookedyClans()) {
            for(Player p: getPlayers()) {
                if(yclans_api == null || yclans_api.getPlayer(p) == null) continue;
                yclans.model.ClanPlayer clan_player = yclans_api.getPlayer(p);
                if(!clan_player.hasClan()) continue;
                yclans_clans.put(clan_player, clan_player.getClan());
                clan_player.getClan().setFriendlyFireAlly(true);
                clan_player.getClan().setFriendlyFireMember(true);
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
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
            }
            for (Player player : getSpectators()) {
                for(String s : enabled_st) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
            }
        }, enable_pvp * 20L);

    }

    public void eliminate(Player captured) {

        // Reviva o jogador morto.
        captured.getInventory().clear();
        captured.setFoodLevel(20);
        captured.setHealth(captured.getMaxHealth());

        if(blue_team.containsKey(captured)) {
            captured.teleport(blue_spawn);
        }else{
            captured.teleport(red_spawn);
        }

        // Mude a armadura do capturado e o paralize.
        dead_players.add(captured);
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

        // Mande a mensagem para o jogador.
        List<String> died_st = config.getStringList("Messages.Died");
        for(String s: died_st) {
            captured.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@time", String.valueOf(respawn_interval))));
        }

        // Depois de alguns segundos, teleporte o capturado para o inicio.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            if(!isHappening()) return;

            dead_players.remove(captured);

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

                captured.teleport(blue_spawn);

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

                captured.teleport(red_spawn);
            }

            for(String item: config.getConfigurationSection("Itens.Inventory").getKeys(false)) {
                captured.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Inventory." + item)));
            }

            // Adicione o jogador á lista de invencíveis e o remova depois de um tempo.
            invincible_players.add(captured);
            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> invincible_players.remove(captured), invincibility_time * 20L);

        }, respawn_interval * 20L);

    }

    public void win(String team) {

        if(!team_selected) return;

        List<String> winners = new ArrayList<>();
        List<String> destroyed = config.getStringList("Messages.Destroyed");

        // Encerre o evento.
        this.stop();

        if(team.equals("blue")) {

            for (Player player : getPlayers()) {
                for(String s: destroyed) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", getConfig().getString("Evento.Title")).replace("@team1", this.red_name).replace("@team2", this.blue_name)));
                }
            }
            for (Player player : getSpectators()) {
                for(String s: destroyed) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", getConfig().getString("Evento.Title")).replace("@team1", this.red_name).replace("@team2", this.blue_name)));
                }
            }

            // Adicionar vitória e dar a tag no LegendChat.
            this.setWinners(blue_team.keySet());

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
                for(String s: destroyed) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", getConfig().getString("Evento.Title")).replace("@team1", this.blue_name).replace("@team2", this.red_name)));
                }
            }
            for (Player player : getSpectators()) {
                for(String s: destroyed) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", getConfig().getString("Evento.Title")).replace("@team1", this.blue_name).replace("@team2", this.red_name)));
                }
            }

            // Adicionar vitória e dar a tag no LegendChat.
            this.setWinners(red_team.keySet());

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
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", String.join(", ", winners)).replace("@name", config.getString("Evento.Title"))));
        }

    }

    @Override
    public void leave(Player p) {

        if(getPlayers().contains(p)) {
            for (Player player : getPlayers()) {
                player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName())));
            }
            for (Player player : getSpectators()) {
                player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName())));
            }
        }

        // Desative o friendly-fire do jogador.
        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            simpleclans_clans.remove(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
            if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) != null) aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(false);
        }

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("massivefactions") && aEventos.getInstance().isHookedMassiveFactions()) {
            massivefactions_factions.remove(MPlayer.get(p));
            if(getClanMembers(p) < 1) MPlayer.get(p).getFaction().setFlag(MFlag.ID_FRIENDLYFIRE, false);
        }

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans") && aEventos.getInstance().isHookedyClans() && !isOpen()) {
            if(yclans_api == null || yclans_api.getPlayer(p) == null || isOpen()) return;
            yclans.model.ClanPlayer clan_player = yclans_api.getPlayer(p);
            if(getClanMembers(p) < 1) {
                if(yclans_clans.get(clan_player) == null) return;
                yclans_clans.get(clan_player).setFriendlyFireMember(false);
                yclans_clans.get(clan_player).setFriendlyFireAlly(false);
                yclans_clans.remove(clan_player);
            }
        }

        blue_team.remove(p);
        red_team.remove(p);
        if(blue_team.size() == 0) win("red");
        if(red_team.size() == 0) win("blue");

        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);
        this.remove(p);
    }

    @Override
    public void stop() {

        // Remova os nexus.
        blue_nexus.remove();
        red_nexus.remove();

        // Remova a armadura dos jogadores.
        for(Player p: getPlayers()) {
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
            p.getInventory().setBoots(null);
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.SLOW);
        }

        // Desative o friendly-fire dos jogadores.
        for (ClanPlayer p : simpleclans_clans) {
            p.setFriendlyFire(false);
        }

        for(MPlayer p: massivefactions_factions.keySet()) {
            p.getFaction().setFlag(MFlag.ID_FRIENDLYFIRE, false);
        }

        for(yclans.model.ClanPlayer p: yclans_clans.keySet()) {
            p.getClan().setFriendlyFireMember(false);
            p.getClan().setFriendlyFireAlly(false);
        }

        simpleclans_clans.clear();
        massivefactions_factions.clear();
        yclans_clans.clear();

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    public HashMap<Player, Integer> getBlueTeam() { return this.blue_team; }
    public HashMap<Player, Integer> getRedTeam() { return this.red_team; }
    public List<Player> getDeadPlayers() { return this.dead_players; }
    public List<Player> getInvinciblePlayers() { return this.invincible_players; }

    public String getBlueTeamName() { return blue_name; }
    public String getRedTeamName() { return red_name; }

    public Entity getBlueNexus() { return blue_nexus; }
    public Entity getRedNexus() { return red_nexus; }
    public int getNexusDamage() { return this.damage; }
    public String getNexusName() { return this.nexus_name; }

    public int getBlueNexusHealth() { return this.blue_nexus_health; }
    public int getRedNexusHealth() { return this.red_nexus_health; }
    public void setBlueNexusHealth(int health) { this.blue_nexus_health = health; }
    public void setRedNexusHealth(int health) { this.red_nexus_health = health; }

    public boolean isPvPEnabled() { return this.pvp_enabled; }

    private int getClanMembers(Player p) {

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("massivefactions")) {
            return (int) massivefactions_factions.keySet()
                    .stream()
                    .filter(map -> map.getFaction() == MPlayer.get(p).getFaction())
                    .count();
        }

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans")) {
            return (int) yclans_clans.keySet()
                    .stream()
                    .filter(map -> map.getClan() == yclans_api.getPlayer(p).getClan())
                    .count();
        }

        return -1;
    }
}
