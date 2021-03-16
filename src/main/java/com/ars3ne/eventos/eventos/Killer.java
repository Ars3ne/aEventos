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
import com.ars3ne.eventos.listeners.eventos.KillerListener;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public class Killer extends Evento {

    private final YamlConfiguration config;
    private final KillerListener listener = new KillerListener();

    private final int enable_pvp, pickup_time;
    private boolean pvp_enabled = false;

    private final List<ClanPlayer> clans = new ArrayList<>();

    public Killer(YamlConfiguration config) {
        super(config);
        this.config = config;
        this.enable_pvp = config.getInt("Evento.Time");
        this.pickup_time = config.getInt("Evento.Pickup time");
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

        // Mande a mensagem de que o PvP será ativado.
        List<String> starting_st = config.getStringList("Messages.Enabling");

        for (Player player : getPlayers()) {
            for(String s : starting_st) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(enable_pvp)).replace("@name", config.getString("Evento.Title")));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : starting_st) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(enable_pvp)).replace("@name", config.getString("Evento.Title")));
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

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);

        this.remove(p);
    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", getConfig().getString("Evento.Title")));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Mande uma mensagem para o vencedor sobre o tempo para pegar os drops.
        List<String> pickup_st = config.getStringList("Messages.Pickup");
        for(String s: pickup_st) {
            p.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(pickup_time)).replace("@name", config.getString("Evento.Title")));
        }

        // Depois do tempo especificado na config, encerre o evento.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            if(!isHappening()) return;

            // Encerre o evento.
            this.stop();

            // Execute todos os comandos de vitória.
            List<String> commands = config.getStringList("Rewards.Commands");
            for(String s : commands) {
                aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), s.replace("@winner", p.getName()));
            }

        }, pickup_time * 20L);

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

    public boolean isPvPEnabled() { return this.pvp_enabled; }

}
