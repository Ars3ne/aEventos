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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
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
        this.elimination = false;

        switch(type) {
            case SPLEEF: case BATATA_QUENTE: case FIGHT: case KILLER: case SUMO: case ANVIL:
                this.elimination = true;
                break;
        }

    }

    public void startCall() {

        this.happening = true;
        this.open = true;

        EventoStartingEvent starting = new EventoStartingEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(starting);

        new BukkitRunnable() {

            int calls = config.getInt("Evento.Calls");

            @Override
            public void run() {

                if (!Evento.this.isHappening()){
                    cancel();
                    return;
                }

                if (calls >= 0){

                    if(!Evento.this.happening) {
                        cancel();
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
        }.runTaskTimer(aEventos.getInstance(), 0, config.getInt("Evento.Calls interval") * 20L);
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

    public void stop() {

    }

    public void removePlayers() {

        this.happening = false;
        this.open = false;

        for (Player player : players) {
            if(this.empty_inventory) player.getInventory().clear();
            if(!this.open && !this.win.contains(player)) {
                PlayerLoseEvent lose = new PlayerLoseEvent(player, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
                Bukkit.getPluginManager().callEvent(lose);
            }
            this.teleport(player, "exit");
        }

        for (Player player : spectators) {
            this.teleport(player, "exit");
        }

        EventoStopEvent stop = new EventoStopEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(stop);
        
        aEventos.getEventoManager().startEvento(EventoType.NONE, null);
    }

    public void join(Player p) {

        p.setFoodLevel(20);
        players.add(p);
        this.teleport(p, "lobby");

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

    public void remove(Player p) {

        players.remove(p);
        spectators.remove(p);
        this.teleport(p, "exit");

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

    private void teleport(Player p, String location) {
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
