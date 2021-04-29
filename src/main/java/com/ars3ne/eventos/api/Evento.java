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

package com.ars3ne.eventos.api;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.events.*;
import com.ars3ne.eventos.hooks.BungeecordHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evento implements EventoInterface{

    private final List<Player> players = new ArrayList<>();
    private final List<Player> spectators = new ArrayList<>();
    private final List<Player> win = new ArrayList<>();
    
    private final EventoType type;
    private boolean happening;
    private boolean open;
    private final boolean allow_spectator;
    private final boolean empty_inventory;
    private boolean elimination;
    private final boolean count_participation;
    private final boolean count_win;
    private final boolean bungeecord_enabled;
    private final String permission;

    private final YamlConfiguration config;

    public Evento(YamlConfiguration config) {

        this.config = config;
        type = EventoType.getEventoType(config.getString("Evento.Type"));
        this.allow_spectator = config.getBoolean("Evento.Spectator mode");
        this.empty_inventory = config.getBoolean("Evento.Empty inventory");
        this.permission = config.getString("Evento.Permission");
        this.count_participation = config.getBoolean("Evento.Count participation");
        this.count_win = config.getBoolean("Evento.Count victory");
        this.bungeecord_enabled = aEventos.getInstance().getConfig().getBoolean("Bungeecord.Enabled");
        this.elimination = false;

        switch(type) {
            case SPLEEF: case BATATA_QUENTE: case FIGHT: case KILLER: case SUMO: case QUIZ: case ANVIL:
                this.elimination = true;
                break;
        }

    }

    public void startCall() {

        this.happening = true;
        this.open = true;

        EventoStartingEvent starting = new EventoStartingEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(starting);

        if(this.bungeecord_enabled && config.getString("Locations.Server") != null){
            BungeecordHook.startEvento(type.toString(), config.getString("filename").substring(0, config.getString("filename").length() - 4));
        }

        new BukkitRunnable() {

            int calls = config.getInt("Evento.Calls");

            @Override
            public void run() {

                if (!Evento.this.isHappening()){
                    cancel();
                    return;
                }

                if (!Evento.this.isOpen()){
                    cancel();
                    return;
                }

                if (calls >= 0){

                    if(!Evento.this.happening) {
                        cancel();
                    }

                    if (!Evento.this.isOpen()){
                        cancel();
                        return;
                    }

                    List<String> broadcast_messages = config.getStringList("Messages.Broadcast");
                    for(String s : broadcast_messages) {
                        aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@players", String.valueOf(Evento.this.players.size())).replace("@broadcasts", String.valueOf(calls)).replace("@name", config.getString("Evento.Title")));
                    }

                    calls--;
                }else if(Evento.this.happening){
                    cancel();
                    if (Evento.this.players.size() >= config.getInt("Evento.Mininum players")){
                        Evento.this.open = false;

                        List<String> broadcast_messages = config.getStringList("Messages.Start");
                        for(String s : broadcast_messages) {
                            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                        }

                        for (Player player : players) {
                            Evento.this.teleport(player, "entrance");
                        }

                        if(Evento.this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")){
                            BungeecordHook.startingEvento(type.toString(), config.getString("filename").substring(0, config.getString("filename").length() - 4));
                        }

                        start();
                        EventoStartedEvent start = new EventoStartedEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
                        Bukkit.getPluginManager().callEvent(start);

                    } else {

                        if(Evento.this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")){
                            BungeecordHook.stopEvento("noplayers");
                        }

                        List<String> broadcast_messages = config.getStringList("Messages.No players");
                        for(String s : broadcast_messages) {
                            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                        }

                        Evento.this.stop();

                    }
                }
            }
        }.runTaskTimer(aEventos.getInstance(), 0, config.getInt("Evento.Calls interval") * 20L);
    }

    public void startBungeecord() {
        if(this.open && this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")){

            if (Evento.this.players.size() >= config.getInt("Evento.Mininum players")){
                Evento.this.open = false;

                List<String> broadcast_messages = config.getStringList("Messages.Start");
                for(String s : broadcast_messages) {
                    aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }

                for (Player player : players) {
                    Evento.this.teleport(player, "entrance");
                }

                BungeecordHook.startingEvento(type.toString(), config.getString("filename").substring(0, config.getString("filename").length() - 4));
                start();
                EventoStartedEvent start = new EventoStartedEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
                Bukkit.getPluginManager().callEvent(start);

            } else {

                List<String> broadcast_messages = config.getStringList("Messages.No players");
                for(String s : broadcast_messages) {
                    aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }

                Evento.this.stop();

            }

        }
    }

    public void start() {

    }

    public void winner(Player p) {

    }

    public void setWinner(Player p) {

        if(!this.count_win) return;

        List<String> winners = new ArrayList<>();
        winners.add(p.getUniqueId().toString());
        this.win.add(p);
        
        aEventos.getConnectionManager().insertUser(p.getUniqueId());
        aEventos.getConnectionManager().addWin(config.getString("filename").substring(0, config.getString("filename").length() - 4), p.getUniqueId());

        aEventos.getConnectionManager().setEventoWinner(config.getString("filename").substring(0, config.getString("filename").length() - 4), winners);
        aEventos.getTagManager().updateTagHolder(config);
        aEventos.updateTags();

        PlayerWinEvent win = new PlayerWinEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(win);

    }

    public void setWinners() {

        if(EventoType.isEventoGuild(type)) return;
        if(!this.count_win) return;
        if(this.elimination) return;

        List<String> winners = new ArrayList<>();

        for (Player p: players) {
            this.win.add(p);
            winners.add(p.getUniqueId().toString());
            aEventos.getConnectionManager().insertUser(p.getUniqueId());
            aEventos.getConnectionManager().addWin(config.getString("filename").substring(0, config.getString("filename").length() - 4), p.getUniqueId());
            PlayerWinEvent win = new PlayerWinEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
            Bukkit.getPluginManager().callEvent(win);
        }

        aEventos.getConnectionManager().setEventoWinner(config.getString("filename").substring(0, config.getString("filename").length() - 4), winners);
        aEventos.getTagManager().updateTagHolder(config);
        aEventos.updateTags();

    }

    public void setWinners(String guild_name, HashMap<OfflinePlayer, Integer> kills) {

        if(!EventoType.isEventoGuild(type)) return;
        if(!this.count_win) return;

        List<String> winners = new ArrayList<>();

        for (Player p: players) {
            this.win.add(p);
            winners.add(p.getUniqueId().toString());
            aEventos.getConnectionManager().insertUser(p.getUniqueId());
            aEventos.getConnectionManager().addWin(config.getString("filename").substring(0, config.getString("filename").length() - 4), p.getUniqueId());
            PlayerWinEvent win = new PlayerWinEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
            Bukkit.getPluginManager().callEvent(win);
        }

        aEventos.getConnectionManager().setEventoGuildWinner(config.getString("filename").substring(0, config.getString("filename").length() - 4), guild_name, kills, winners);        aEventos.getTagManager().updateTagHolder(config);
        aEventos.updateTags();

    }

    public void stop() {

    }

    public void removePlayers() {

        this.happening = false;
        this.open = false;

        if(this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")){
            BungeecordHook.stopEvento("ended");
        }

        for (Player player : players) {
            if(this.empty_inventory) player.getInventory().clear();
            if(!this.open && !this.win.contains(player)) {
                PlayerLoseEvent lose = new PlayerLoseEvent(player, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
                Bukkit.getPluginManager().callEvent(lose);
            }
            this.teleport(player, "exit");
        }

        for (Player player : spectators) {
            player.getInventory().clear();
            this.teleport(player, "exit");
        }

        EventoStopEvent stop = new EventoStopEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(stop);

        aEventos.getCache().updateCache();
        aEventos.getEventoManager().startEvento(EventoType.NONE, null);
    }

    public void joinBungeecord(Player p) {
        if(this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")){
            BungeecordHook.joinEvento(p.getName());
        }else {
            join(p);
        }
    }

    public void join(Player p) {

        p.setFoodLevel(20);
        players.add(p);
        this.teleport(p, "lobby");

        for(PotionEffect potion: p.getActivePotionEffects()) {
            p.removePotionEffect(potion.getType());
        }

        for (Player player : players) {
            player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Joined").replace("&", "§").replace("@player", p.getName()));
        }

        for (Player player : spectators) {
            player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Joined").replace("&", "§").replace("@player", p.getName()));
        }

        PlayerJoinEvent join = new PlayerJoinEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(join);


    }

    public void leave(Player p) {

        if(players.contains(p)) {
            for (Player player : players) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName()));
            }

            for (Player player : spectators) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName()));
            }
        }

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(lose);

        this.remove(p);

    }

    public void leaveBungeecord(Player p) {
        if(!notifyLeave(p)) {
            leave(p);
        }
    }

    public boolean notifyLeave(Player p) {
        if(this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")) {
            BungeecordHook.leaveEvento(p.getName());
            return true;
        }
        return false;
    }

    public void remove(Player p) {

        players.remove(p);
        spectators.remove(p);
        this.teleport(p, "exit");

        for(PotionEffect potion: p.getActivePotionEffects()) {
            p.removePotionEffect(potion.getType());
        }

        if(this.empty_inventory) p.getInventory().clear();

        if(!this.open && this.elimination && players.size() == 1) {
            this.winner(players.get(0));
        }

        if(!this.open && players.size() == 0) {
            List<String> broadcast_messages = config.getStringList("Messages.No winner");
            for(String s : broadcast_messages) {
                aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
            }
            this.stop();
        }

    }

    public void spectate(Player p) {
        p.setFoodLevel(20);
        spectators.add(p);
        this.teleport(p, "spectator");
    }

    public void spectateBungeecord(Player p) {
        if(this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")){
            BungeecordHook.spectateEvento(p.getName());
        }else {
            spectate(p);
        }
    }

    public void executeConsoleCommand(Player p, String command) {

        if(this.bungeecord_enabled && config.getString("Locations.Server") != null && !config.getString("Locations.Server").equals("null")){
            BungeecordHook.executeCommand(p.getName(), command, config.getString("Locations.Server"));
        }else {
            aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), command);
        }

    }

    public YamlConfiguration getConfig() {
        return this.config;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public List<Player> getSpectators() {
        return this.spectators;
    }

    public String getPermission() { return this.permission; }

    public EventoType getType() { return this.type; }

    public boolean isElimination() { return this.elimination; }

    public boolean isHappening() {
        return this.happening;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean isSpectatorAllowed() {
        return this.allow_spectator;
    }

    public boolean requireEmptyInventory() {
        return this.empty_inventory;
    }

    public boolean countParticipation() {
        return this.count_participation;
    }

    public boolean countWin() {
        return this.count_win;
    }

    protected void teleport(Player p, String location) {
        World w;
        double x,y,z;
        float yaw,pitch;

        switch(location) {
            case "lobby":
                w = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Lobby.world"));
                x = config.getDouble("Locations.Lobby.x");
                y = config.getDouble("Locations.Lobby.y");
                z = config.getDouble("Locations.Lobby.z");
                yaw = config.getLong("Locations.Lobby.yaw");
                pitch = config.getLong("Locations.Lobby.pitch");
                p.teleport(new Location(w, x, y, z, yaw, pitch));
                break;
            case "entrance":
                w = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Entrance.world"));
                x = config.getDouble("Locations.Entrance.x");
                y = config.getDouble("Locations.Entrance.y");
                z = config.getDouble("Locations.Entrance.z");
                yaw = config.getLong("Locations.Entrance.yaw");
                pitch = config.getLong("Locations.Entrance.pitch");
                p.teleport(new Location(w, x, y, z, yaw, pitch));
                break;
            case "exit":
                if(this.count_participation && !this.open) {
                    aEventos.getConnectionManager().insertUser(p.getUniqueId());
                    aEventos.getConnectionManager().addParticipation(config.getString("filename").substring(0, config.getString("filename").length() - 4), p.getUniqueId());
                }
                w = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Exit.world"));
                x = config.getDouble("Locations.Exit.x");
                y = config.getDouble("Locations.Exit.y");
                z = config.getDouble("Locations.Exit.z");
                yaw = config.getLong("Locations.Exit.yaw");
                pitch = config.getLong("Locations.Exit.pitch");
                p.teleport(new Location(w, x, y, z, yaw, pitch));
                break;
            case "spectator":
                w = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Spectator.world"));
                x = config.getDouble("Locations.Spectator.x");
                y = config.getDouble("Locations.Spectator.y");
                z = config.getDouble("Locations.Spectator.z");
                yaw = config.getLong("Locations.Spectator.yaw");
                pitch = config.getLong("Locations.Spectator.pitch");
                p.teleport(new Location(w, x, y, z, yaw, pitch));
                break;
        }
    }
}
