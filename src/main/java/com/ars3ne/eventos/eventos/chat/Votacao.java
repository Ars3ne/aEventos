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

package com.ars3ne.eventos.eventos.chat;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.EventoChat;
import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.utils.EventoConfigFile;
import com.ars3ne.eventos.utils.Utils;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class Votacao extends EventoChat {

    private final HashMap<Integer, String> valid_alternatives = new HashMap<>();
    private final HashMap<Player, Integer> votes =  new HashMap<>();

    private final YamlConfiguration config;
    private final ConfigurationSection alternatives;

    private final int total_alternatives;

    public Votacao(YamlConfiguration config) {
        super(config);

        this.config = config;
        alternatives = config.getConfigurationSection("Alternatives");
        this.total_alternatives = config.getInt("Evento.Alternatives");

        if(total_alternatives > alternatives.getKeys(false).size()) {
            Bukkit.getConsoleSender().sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Not enough alternatives").replace("&", "§")));
            stop();
        }

        while(valid_alternatives.size() != total_alternatives) {
            Random random = new Random();
            int id = random.nextInt(alternatives.getKeys(false).size());
            valid_alternatives.put(id, alternatives.getConfigurationSection((String) alternatives.getKeys(false).toArray()[id]).getString("Name"));
        }

    }

    @Override
    public void start() {

        // Se não houveram votos, então inicie um evento aleatório.
        if(votes.size() == 0) {

            List<String> broadcast_messages = config.getStringList("Messages.No votes");
            for(String s : broadcast_messages) {
                aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
            }

            Random random = new Random();
            Object[] keyset = valid_alternatives.keySet().toArray();

            YamlConfiguration config_evento = EventoConfigFile.get(alternatives.getKeys(false).toArray()[(int) keyset[random.nextInt(keyset.length)]].toString());
            if(EventoType.isEventoChat(EventoType.getEventoType(config_evento.getString("Evento.Type")))) {
                aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoChatManager().startEvento(EventoType.getEventoType(config_evento.getString("Evento.Type")), config_evento), 20L);
            }else {
                aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoManager().startEvento(EventoType.getEventoType(config_evento.getString("Evento.Type")), config_evento), 20L);
            }

        }else{

            HashMap<Integer, Integer> total_votes = new HashMap<>();

            for(Integer id: valid_alternatives.keySet()) {
                total_votes.put(id, 0);
            }

            for(Integer vote: votes.values()) {
                total_votes.put(vote, total_votes.get(vote) + 1);
            }

            String name = valid_alternatives.get(Collections.max(total_votes.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey());

            List<String> broadcast_messages = config.getStringList("Messages.Winner");
            for(String s : broadcast_messages) {
                aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", name).replace("@name", config.getString("Evento.Title"))));
            }

            YamlConfiguration config_evento = EventoConfigFile.get(alternatives.getKeys(false).toArray()[Collections.max(total_votes.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey()].toString());
            if(EventoType.isEventoChat(EventoType.getEventoType(config_evento.getString("Evento.Type")))) {
                aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoChatManager().startEvento(EventoType.getEventoType(config_evento.getString("Evento.Type")), config_evento), 20L);
            }else {
                aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoManager().startEvento(EventoType.getEventoType(config_evento.getString("Evento.Type")), config_evento), 20L);
            }

        }

        stop();

    }

    @Override
    public void stop() {
        removePlayers();
    }

    @Override
    public void parseMessage(String s, int calls) {
        s = s.replace("&", "§").replace("@broadcasts", String.valueOf(calls)).replace("@name", config.getString("Evento.Title"));

        for(int i = 1; i <= total_alternatives; i++) {

            if(s.contains("@alternative" + i)) {

                s = s.replace("@alternative" + i, (String) valid_alternatives.values().toArray()[i - 1]);

                TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', s)));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(config.getString("Messages.Hover").replace("&", "§").replace("@alternative", (String) valid_alternatives.values().toArray()[i - 1])).create()));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/evento " + Utils.getKeysByValue(valid_alternatives, (String) valid_alternatives.values().toArray()[i - 1]).toArray()[0]));

                aEventos.getInstance().getServer().spigot().broadcast(component);
                return;

            }

            s = s.replace("@alternative" + i, (String) valid_alternatives.values().toArray()[i - 1]);
        }

        aEventos.getInstance().getServer().broadcastMessage(s);
    }

    @Override
    public void parseCommand(Player p, String[] args) {

        if(votes.containsKey(p)) {
            p.sendMessage(config.getString("Messages.Already voted").replace("&", "§").replace("@name", config.getString("Evento.Title")));
            return;
        }

        try{
            int vote = Integer.parseInt(args[0]);

            if(!valid_alternatives.containsKey(vote)) {
                p.sendMessage(config.getString("Messages.Invalid").replace("&", "§").replace("@name", config.getString("Evento.Title")));
                return;
            }

            votes.put(p, vote);
            p.sendMessage(config.getString("Messages.Voted").replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@alternative", valid_alternatives.get(vote)));

        }catch(NumberFormatException e) {
            p.sendMessage(config.getString("Messages.Invalid").replace("&", "§").replace("@name", config.getString("Evento.Title")));
        }

    }
}
