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
import com.ars3ne.eventos.listeners.eventos.SumoListener;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.factions.entity.MPlayer;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import yclans.api.yClansAPI;
import yclans.model.Clan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sumo extends Evento {

    private final YamlConfiguration config;
    private final SumoListener listener = new SumoListener();

    private yClansAPI yclans_api;

    private final ArrayList<ClanPlayer> simpleclans_clans = new ArrayList<>();
    private final HashMap<MPlayer, Faction> massivefactions_factions = new HashMap<>();
    private final HashMap<yclans.model.ClanPlayer, Clan> yclans_clans = new HashMap<>();

    public Sumo(YamlConfiguration config) {

        super(config);
        this.config = config;

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans")) {
            yclans_api = yClansAPI.yclansapi;
        }

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

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

        // De o stick para os jogadores.
        ItemStack stick = new ItemStack(Material.STICK, 1);
        ItemMeta meta = stick.getItemMeta();
        meta.addEnchant(Enchantment.KNOCKBACK, 5, true);
        stick.setItemMeta(meta);

        for(Player p: getPlayers()) {
            p.getInventory().addItem(stick);
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
        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("simpleclans") && aEventos.getInstance().getSimpleClans() != null) {
            simpleclans_clans.remove(aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p));
            aEventos.getInstance().getSimpleClans().getClanManager().getClanPlayer(p).setFriendlyFire(false);
        }

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("massivefactions") && aEventos.getInstance().isHookedMassiveFactions()) {
            massivefactions_factions.remove(MPlayer.get(p));
            if(getClanMembers(p) < 1) MPlayer.get(p).getFaction().setFlag(MFlag.ID_FRIENDLYFIRE, false);
        }

        if(aEventos.getInstance().getConfig().getString("Hook").equalsIgnoreCase("yclans") && aEventos.getInstance().isHookedyClans()) {
            if(yclans_api == null || yclans_api.getPlayer(p) == null) return;
            yclans.model.ClanPlayer clan_player = yclans_api.getPlayer(p);
            yclans_clans.remove(clan_player);
            if(getClanMembers(p) < 1) {
                yclans_clans.get(clan_player).setFriendlyFireMember(false);
                yclans_clans.get(clan_player).setFriendlyFireAlly(false);
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

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();

    }

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
