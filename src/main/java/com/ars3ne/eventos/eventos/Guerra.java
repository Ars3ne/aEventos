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
import com.ars3ne.eventos.api.events.PlayerJoinEvent;
import com.ars3ne.eventos.api.events.PlayerLoseEvent;
import com.ars3ne.eventos.hooks.BungeecordHook;
import com.ars3ne.eventos.listeners.eventos.GuerraListener;
import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.messages.ActionBar;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import yclans.api.yClansAPI;

import java.util.*;
import java.util.stream.Collectors;

public class Guerra extends Evento {

    private final YamlConfiguration config;
    private final GuerraListener listener = new GuerraListener();

    private yClansAPI yclans_api;

    private final int enable_pvp, pickup_time, min_guilds, max_players;
    private boolean pvp_enabled, ended = false;
    private final boolean actionbar_enabled, border_enabled, ffa, defined_items;
    private final String hook;

    private final HashMap<ClanPlayer, Clan> simpleclans_clan_participants = new HashMap<>();
    private final HashMap<MPlayer, Faction> massivefactions_factions_participants = new HashMap<>();
    private final HashMap<yclans.model.ClanPlayer, yclans.model.Clan> yclans_clan_participants = new HashMap<>();

    private final HashMap<OfflinePlayer, Integer> kills = new HashMap<>();

    private final WorldBorder border;
    private final int border_delay, border_damage, border_time, border_size;
    private int task, task2, task3;

    public Guerra(YamlConfiguration config) {
        super(config);

        this.config = config;
        this.enable_pvp = config.getInt("Evento.Time");
        this.pickup_time = config.getInt("Evento.Pickup time");
        this.actionbar_enabled = config.getBoolean("Actionbar.Enabled");
        this.border_enabled = config.getBoolean("Border.Enabled");
        this.defined_items = config.getBoolean("Itens.Enabled");
        this.ffa = config.getBoolean("Evento.FFA");
        this.hook = aEventos.getInstance().getConfig().getString("Hook");
        this.min_guilds = config.getInt("Evento.Minimum guilds");
        this.max_players = config.getInt("Evento.Maximum per guild");
        this.border_size = config.getInt("Border.Size");
        this.border_delay = config.getInt("Border.Delay");
        this.border_time = config.getInt("Border.Time");
        this.border_damage = config.getInt("Border.Damage");

        this.border = Bukkit.getWorld(config.getString("Locations.Entrance.world")).getWorldBorder();

        if(hook.equalsIgnoreCase("yclans")) {
            yclans_api = yClansAPI.yclansapi;
        }

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Se o servidor tiver SimpleClans, então ative o friendly fire.
        if(hook.equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            for(Player p: getPlayers()) {
                if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) != null) {
                    simpleclans_clan_participants.put(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p), aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).getClan());
                    if(ffa) aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(true);
                }
            }

            // Se a quantidade de clans for menor do que o mínimo, então cancele o evento.
            if(getTotalGuilds() < this.min_guilds) {

                if(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Enabled") && config.getString("Locations.Server") != null){
                    BungeecordHook.stopEvento("noguilds");
                }

                List<String> no_guild = config.getStringList("Messages.No guilds");
                for(String s : no_guild) {
                    Bukkit.broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
                stop();
                return;
            }
        }

        // Se o servidor tiver MassiveFactions, então verifique a quantidade de facções.
        if(hook.equalsIgnoreCase("massivefactions") && aEventos.getInstance().isHookedMassiveFactions()) {

            for(Player p: getPlayers()) {
                    massivefactions_factions_participants.put(MPlayer.get(p), MPlayer.get(p).getFaction());
                    if(ffa) aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(true);
            }

            if(getTotalGuilds() < this.min_guilds) {
                List<String> no_guild = config.getStringList("Messages.No guilds");
                for(String s : no_guild) {
                    Bukkit.broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
                stop();
                return;
            }

        }

        // Se o servidor tiver yClans, então verifique a quantidade de clãs.
        if(hook.equalsIgnoreCase("yclans") && aEventos.getInstance().isHookedyClans()) {

            for(Player p: getPlayers()) {
                if(yclans_api == null || yclans_api.getPlayer(p) == null) continue;
                yclans.model.ClanPlayer clan_player = yclans_api.getPlayer(p);
                if(!clan_player.hasClan()) continue;
                yclans_clan_participants.put(clan_player, clan_player.getClan());
                if(ffa) clan_player.getClan().setFriendlyFireAlly(true);
                if(ffa) clan_player.getClan().setFriendlyFireMember(true);
            }
        }

        // Se os itens setados estão ativados, então os obtenha.
        if(defined_items) {
            for(Player p: getPlayers()) {

                p.getInventory().setHelmet(XItemStack.deserialize(config.getConfigurationSection("Itens.Helmet")));
                p.getInventory().setChestplate(XItemStack.deserialize(config.getConfigurationSection("Itens.Chestplate")));
                p.getInventory().setLeggings(XItemStack.deserialize(config.getConfigurationSection("Itens.Leggings")));
                p.getInventory().setBoots(XItemStack.deserialize(config.getConfigurationSection("Itens.Boots")));

                for(String item: config.getConfigurationSection("Itens.Inventory").getKeys(false)) {
                    p.getInventory().setItem(Integer.parseInt(item), XItemStack.deserialize(config.getConfigurationSection("Itens.Inventory." + item)));
                }

            }
        }
        // Mande a mensagem de que o PvP será ativado.
        List<String> starting_st = config.getStringList("Messages.Enabling");

        for (Player player : getPlayers()) {
            for(String s : starting_st) {
                player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@time", String.valueOf(enable_pvp)).replace("@name", config.getString("Evento.Title"))));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : starting_st) {
                player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@time", String.valueOf(enable_pvp)).replace("@name", config.getString("Evento.Title"))));
            }
        }

        // Faça um countdown na action bar.
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(aEventos.getInstance(), new Runnable() {

            int run = 0;
            @Override
            public void run() {
                if(!isHappening()) Bukkit.getScheduler().cancelTask(task);
                if(enable_pvp - run <= 0) Bukkit.getScheduler().cancelTask(task);
                if(!actionbar_enabled) Bukkit.getScheduler().cancelTask(task);
                if(isPvPEnabled()) Bukkit.getScheduler().cancelTask(task);

                Iterator<Player> player_iterator = getPlayers().iterator();
                if (player_iterator.hasNext()) {
                    do {
                        Player player = player_iterator.next();
                        ActionBar.sendActionBar(player, IridiumColorAPI.process(config.getString("Actionbar.Enabling PvP").replace("&", "§").replace("@time", String.valueOf(enable_pvp - run))));
                    } while (player_iterator.hasNext());
                }
                Iterator<Player> spectator_iterator = getSpectators().iterator();
                if (spectator_iterator.hasNext()) {
                    do {
                        Player player = spectator_iterator.next();
                        ActionBar.sendActionBar(player, IridiumColorAPI.process(config.getString("Actionbar.Enabling PvP").replace("&", "§").replace("@time", String.valueOf(enable_pvp - run))));
                    } while (spectator_iterator.hasNext());
                }

                run+=1;
            }

        }, 0L, 20L);

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


            // Mande a actionbar para os jogadores e espectadores.
            task2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(aEventos.getInstance(), () -> {
                if(!isHappening()) Bukkit.getScheduler().cancelTask(task2);
                if(!actionbar_enabled) Bukkit.getScheduler().cancelTask(task2);
                if(ended) Bukkit.getScheduler().cancelTask(task2);
                if(!isPvPEnabled()) Bukkit.getScheduler().cancelTask(task2);

                for(Player player: getPlayers()) {
                    ActionBar.sendActionBar(player, IridiumColorAPI.process(config.getString("Actionbar.Normal").replace("&", "§").replace("@guilds", String.valueOf(getTotalGuilds())).replace("@enemies", String.valueOf(getEnemiesTotal(player))).replace("@remeaning", String.valueOf(getPlayers().size()))));
                }

                for(Player player: getSpectators()) {
                    ActionBar.sendActionBar(player, IridiumColorAPI.process(config.getString("Actionbar.Spectator").replace("&", "§").replace("@guilds", String.valueOf(getTotalGuilds())).replace("@enemies", String.valueOf(getEnemiesTotal(player))).replace("@remeaning", String.valueOf(getPlayers().size()))));
                }

            }, 0L, 40L);

            // Se a borda estiver ativa, então a diminua.
            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

                if(!isHappening()) return;
                if(ended) return;
                if(!border_enabled) return;

                border.setDamageAmount(border_damage);
                border.setSize(1, border_time);

            }, border_delay * 20L);

        }, enable_pvp * 20L);

    }

    @Override
    public void join(Player p) {

        if(aEventos.getInstance().getConfig().getBoolean("Bungeecord.Enabled") && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")) {
            BungeecordHook.joinEvento(p.getName());
        }

        // Se o clã do jogador já bateu o limite de jogadores máximo, retorne.
        if(getTotalGuildPlayers(p) > this.max_players) {
            p.sendMessage(IridiumColorAPI.process(config.getString("Messages.Maximum").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
            return;
        }

        // Se o jogador não está em um clan ou facção, retorne.
        if(hook.equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) == null) {
                p.sendMessage(IridiumColorAPI.process(config.getString("Messages.No guild").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                return;
            }
        }

        if(hook.equalsIgnoreCase("massivefactions")) {
            if(!MPlayer.get(p).hasFaction()) {
                p.sendMessage(IridiumColorAPI.process(config.getString("Messages.No guild").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                return;
            }
        }

        if(hook.equalsIgnoreCase("yclans")) {
            if(yclans_api == null || yclans_api.getPlayer(p) == null || !yclans_api.getPlayer(p).hasClan()) {
                p.sendMessage(IridiumColorAPI.process(config.getString("Messages.No guild").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                return;
            }
        }

        p.setFoodLevel(20);
        getPlayers().add(p);
        teleport(p, "lobby");

        for(PotionEffect potion: p.getActivePotionEffects()) {
            p.removePotionEffect(potion.getType());
        }

        for (Player player : getPlayers()) {
            player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Joined").replace("&", "§").replace("@player", p.getName())));
        }

        for (Player player : getSpectators()) {
            player.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Joined").replace("&", "§").replace("@player", p.getName())));
        }

        PlayerJoinEvent join = new PlayerJoinEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(join);

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

        this.eliminate(p);

    }

    public void eliminate(Player p) {

        // Desative o friendly-fire do jogador.
        if(hook.equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            if(simpleclans_clan_participants.containsKey(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p))) {
                simpleclans_clan_participants.remove(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
                if(ffa) aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(false);
            }
        }

        if(hook.equalsIgnoreCase("massivefactions") && aEventos.getInstance().isHookedMassiveFactions()) {
            if(ffa) aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(false);
            massivefactions_factions_participants.remove(MPlayer.get(p));
        }

        if(hook.equalsIgnoreCase("yclans") && aEventos.getInstance().isHookedyClans()) {
            if(yclans_api == null || yclans_api.getPlayer(p) == null) return;
            yclans.model.ClanPlayer clan_player = yclans_api.getPlayer(p);
            yclans_clan_participants.remove(clan_player);
            if(ffa) clan_player.getClan().setFriendlyFireAlly(false);
            if(ffa) clan_player.getClan().setFriendlyFireMember(false);
        }

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);

        // Se os itens forem setados, então limpe o inventário do jogador.
        if(defined_items) {
            p.getInventory().clear();
            p.getInventory().setHelmet(null);
            p.getInventory().setChestplate(null);
            p.getInventory().setLeggings(null);
            p.getInventory().setBoots(null);
        }

        this.remove(p);

        // Se sobrou apenas uma facção ou clan, então encerre o evento.
        if(getTotalGuilds() == 1) {
            this.win();
        }

    }
    public void win() {

        this.ended = true;

        if(border_enabled) border.setSize(border_size, 0);

        List<String> winners = new ArrayList<>();

        // Adicionar vitória e dar a tag no LegendChat.

        String winner_guild = null;

        if(hook.equalsIgnoreCase("simpleclans")) {
            this.setWinners(simpleclans_clan_participants.values().stream().findFirst().get().getTag(), this.kills);
            winner_guild = simpleclans_clan_participants.values().stream().findFirst().get().getTag();
        }

        if(hook.equalsIgnoreCase("massivefactions")) {
            this.setWinners(massivefactions_factions_participants.values().stream().findFirst().get().getName(), this.kills);
            winner_guild = massivefactions_factions_participants.values().stream().findFirst().get().getName();
        }

        if(hook.equalsIgnoreCase("yclans")) {
            this.setWinners(yclans_clan_participants.values().stream().findFirst().get().getTag(), this.kills);
            winner_guild = yclans_clan_participants.values().stream().findFirst().get().getTag();
        }

        // Mande a mensagem da coleta de itens.
        List<String> pickup_st = config.getStringList("Messages.Pickup");
        for(Player p: getPlayers()) {

            for(String s: pickup_st) {
                p.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@time", String.valueOf(pickup_time)).replace("@name", config.getString("Evento.Title"))));
            }

            // Adicione o nome á lista de vencedores.
            winners.add(p.getName());
        }

        // Mande a mensagem de vitória para o servidor.
        List<String> broadcast_messages = this.config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            assert winner_guild != null;
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", String.join(", ", winners)).replace("@guild", winner_guild).replace("@name", config.getString("Evento.Title"))));
        }

        // Mande a actionbar para os jogadores e espectadores.
        task3 = Bukkit.getScheduler().scheduleSyncRepeatingTask(aEventos.getInstance(), new Runnable() {

            int run = 0;
            @Override
            public void run() {
                if(!isHappening()) Bukkit.getScheduler().cancelTask(task3);
                if(pickup_time - run <= 0) Bukkit.getScheduler().cancelTask(task3);
                if(!actionbar_enabled) Bukkit.getScheduler().cancelTask(task3);

                for(Player player: getPlayers()) {
                    ActionBar.sendActionBar(player, IridiumColorAPI.process(config.getString("Actionbar.Pickup").replace("&", "§").replace("@time", String.valueOf(pickup_time - run))));
                }

                run+=1;
            }


        }, 0L, 20L);

        // Depois do tempo especificado na config, encerre o evento.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            if(!isHappening()) return;

            // Encerre o evento.
            this.stop();

            // Obtenha todos os jogadores restantes e os entregue as recompensas.
            for(Player p: getPlayers()) {

                // Execute os comandos de vitória
                List<String> commands = this.config.getStringList("Rewards.Commands");
                for(String s : commands) {
                    executeConsoleCommand(p, s.replace("@winner", p.getName()));
                }

            }

            // Execute os comandos do top kills.
            ConfigurationSection top_kills_commands = config.getConfigurationSection("Rewards.Top kills");

            HashMap<OfflinePlayer, Integer> sorted = kills.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            if(top_kills_commands.getBoolean("Winner guild")) {

                for(OfflinePlayer p: sorted.keySet()) {

                    if(hook.equalsIgnoreCase("simpleclans")) {
                        if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).getClan() != simpleclans_clan_participants.values().toArray()[0]) {
                            sorted.remove(p);
                        }
                    }

                    if(hook.equalsIgnoreCase("massivefactions")) {
                        if(MPlayer.get(p).getFaction() != massivefactions_factions_participants.values().toArray()[0]) {
                            sorted.remove(p);
                        }
                    }

                    if(hook.equalsIgnoreCase("yclans")) {
                        if(yclans_api == null || yclans_api.getPlayer((Player) p) == null || !yclans_api.getPlayer((Player) p).hasClan()) continue;
                        if(yclans_api.getPlayer((Player) p).getClan() != yclans_clan_participants.values().toArray()[0]) {
                            sorted.remove(p);
                        }
                    }
                }

            }

            for(int i = 0; i < sorted.size(); i++) {

                List<String> commands = top_kills_commands.getStringList(String.valueOf(i + 1));
                if(commands == null) continue;
                OfflinePlayer p = (OfflinePlayer) sorted.keySet().toArray()[i];
                for(String s : commands) {
                    executeConsoleCommand((Player) p, s.replace("@topkiller", p.getName()));
                }

            }

        }, pickup_time * 20L);

    }

    @Override
    public void stop() {

        // Coloque a borda no seu lugar.
        if(border_enabled) border.setSize(border_size, 0);

        // Se o evento for de itens setados, limpe o inventário dos jogadores.
        if(defined_items) {
            for(Player p: getPlayers()) {
                p.getInventory().clear();
                p.getInventory().setHelmet(null);
                p.getInventory().setChestplate(null);
                p.getInventory().setLeggings(null);
                p.getInventory().setBoots(null);
            }
        }

        // Desative o friendly-fire dos jogadores.
        for (ClanPlayer p : simpleclans_clan_participants.keySet()) {
            p.setFriendlyFire(false);
        }

        for(yclans.model.ClanPlayer p: yclans_clan_participants.keySet()) {
            if(!ffa) continue;
            yclans_clan_participants.get(p).setFriendlyFireMember(false);
            yclans_clan_participants.get(p).setFriendlyFireAlly(false);
        }

        simpleclans_clan_participants.clear();
        massivefactions_factions_participants.clear();
        yclans_clan_participants.clear();

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    public boolean isPvPEnabled() { return this.pvp_enabled; }

    public HashMap<OfflinePlayer, Integer> getKills() { return this.kills; }

    private int getEnemiesTotal(Player p) {

        if(hook.equalsIgnoreCase("simpleclans")) {
            if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) == null) return 0;
            return (int) simpleclans_clan_participants.keySet()
                    .stream()
                    .filter(map -> map.getClan() != aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).getClan())
                    .count();
        }

        if(hook.equalsIgnoreCase("massivefactions")) {
            if(MPlayer.get(p).getFaction() == null) return 0;
            return (int) massivefactions_factions_participants.keySet()
                    .stream()
                    .filter(map -> map.getFaction() != MPlayer.get(p).getFaction())
                    .count();
        }

        if(hook.equalsIgnoreCase("yclans")) {
            if(yclans_api.getPlayer(p) == null || !yclans_api.getPlayer(p).hasClan()) return 0;
            return (int) yclans_clan_participants.keySet()
                    .stream()
                    .filter(map -> map.getClan() != yclans_api.getPlayer(p).getClan())
                    .count();
        }

        return -1;
    }

    private int getTotalGuilds() {


        if(hook.equalsIgnoreCase("simpleclans")) {
            return (int) simpleclans_clan_participants.values().stream().distinct().count();
        }

        if(hook.equalsIgnoreCase("massivefactions")) {
            return (int) massivefactions_factions_participants.values().stream().distinct().count();
        }

        if(hook.equalsIgnoreCase("yclans")) {
            return (int) yclans_clan_participants.values().stream().distinct().count();
        }

        return -1;

    }

    private int getTotalGuildPlayers(Player p) {

        if(hook.equalsIgnoreCase("simpleclans")) {
            if(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p) == null) return 0;
            return (int) simpleclans_clan_participants.keySet()
                    .stream()
                    .filter(map -> map.getClan() == aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).getClan())
                    .count();
        }

        if(hook.equalsIgnoreCase("massivefactions")) {
            if(MPlayer.get(p).getFaction() == null) return 0;
            return (int) massivefactions_factions_participants.keySet()
                    .stream()
                    .filter(map -> map.getFaction() == MPlayer.get(p).getFaction())
                    .count();
        }

        if(hook.equalsIgnoreCase("yclans")) {
            if(yclans_api.getPlayer(p) == null || !yclans_api.getPlayer(p).hasClan()) return 0;
            return (int) yclans_clan_participants.keySet()
                    .stream()
                    .filter(map -> map.getClan() == yclans_api.getPlayer(p).getClan())
                    .count();
        }

        return -1;
    }

}
