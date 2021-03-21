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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class EventoChat implements EventoInterface{

    private final EventoType type;
    private boolean happening;
    private final boolean count_participation;
    private final boolean count_win;
    private final String permission;

    private final YamlConfiguration config;

    public EventoChat(YamlConfiguration config) {

        this.config = config;
        type = EventoType.getEventoType(config.getString("Evento.Type"));
        this.permission = config.getString("Evento.Permission");

        if (type == EventoType.VOTACAO) {
            this.count_participation = false;
            this.count_win = false;
        } else {
            this.count_participation = config.getBoolean("Evento.Count participation");
            this.count_win = config.getBoolean("Evento.Count victory");
        }

    }

    public void startCall() {

        this.happening = true;

        EventoStartingEvent starting = new EventoStartingEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(starting);

        new BukkitRunnable() {

            int calls = config.getInt("Evento.Calls");

            @Override
            public void run() {

                if (!EventoChat.this.isHappening()){
                    cancel();
                    return;
                }

                if (calls >= 0){

                    if(!EventoChat.this.happening) {
                        cancel();
                    }

                    List<String> broadcast_messages = config.getStringList("Messages.Broadcast");
                    for(String s : broadcast_messages) {
                        parseMessage(s, calls);
                        //aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@broadcasts", String.valueOf(calls)).replace("@name", config.getString("Evento.Title")));
                    }

                    calls--;
                }else if(EventoChat.this.happening){

                    cancel();
                    start();

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

        /*List<String> winners = new ArrayList<>();
        winners.add(p.getUniqueId().toString());
        this.win.add(p);
        
        aEventos.getConnectionManager().insertUser(p.getUniqueId());
        aEventos.getConnectionManager().addWin(config.getString("filename").substring(0, config.getString("filename").length() - 4), p.getUniqueId());

        aEventos.getConnectionManager().setEventoWinner(config.getString("filename").substring(0, config.getString("filename").length() - 4), winners);
        aEventos.getTagManager().updateTagHolder(config);
        aEventos.updateTags();

        PlayerWinEvent win = new PlayerWinEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(win);
        */

    }

    public void setWinners() {

    }

    public void stop() {

    }

    public void removePlayers() {

        this.happening = false;

        EventoStopEvent stop = new EventoStopEvent(config.getString("filename").substring(0, config.getString("filename").length() - 4), type);
        Bukkit.getPluginManager().callEvent(stop);

        aEventos.getEventoChatManager().startEvento(EventoType.NONE, null);
    }

    public void join(Player p) {
    }

    public void leave(Player p) {

    }

    public void remove(Player p) {

    }

    public void spectate(Player p) {

    }

    public void parseMessage(String s, int calls) {
        aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@broadcasts", String.valueOf(calls)).replace("@name", config.getString("Evento.Title")));
    }

    public void parseCommand(Player p, String[] args) {

    }

    public YamlConfiguration getConfig() {
        return this.config;
    }

    public List<Player> getPlayers() {
        return null;
    }

    public List<Player> getSpectators() {
        return null;
    }

    public String getPermission() { return this.permission; }

    public EventoType getType() { return this.type; }

    public boolean isElimination() { return false; }

    public boolean isHappening() {
        return this.happening;
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isSpectatorAllowed() {
        return false;
    }

    public boolean requireEmptyInventory() {
        return false;
    }

    public boolean countParticipation() {
        return this.count_participation;
    }

    public boolean countWin() {
        return this.count_win;
    }

}
