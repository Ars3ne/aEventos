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
import com.ars3ne.eventos.utils.NumberFormatter;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Sorteio extends EventoChat {

    private final YamlConfiguration config;

    private final List<Player> players = new ArrayList<>();
    private final long cost;
    private boolean has_winner = false;

    public Sorteio(YamlConfiguration config) {

        super(config);

        this.config = config;
        this.cost = config.getLong("Evento.Cost");

    }

    @Override
    public void start() {

        // Se ninguém apostou no bolão, então encerre o evento sem vencedores.
        if(players.size() == 0) {

            List<String> broadcast_messages = config.getStringList("Messages.No winner");
            for(String s : broadcast_messages) {
                aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
            }

            stop();

        }else {
            // Pegue um jogador aleatório e o defina como vencedor.
            Random random = new Random();
            winner(players.get(random.nextInt(players.size())));
        }

    }

    @Override
    public void stop() {
        if(!has_winner) {
            for(Player p: players) {
                aEventos.getInstance().getEconomy().depositPlayer(p, cost);
            }
        }
        players.clear(); // Parece redundante, mas não é.
        removePlayers();
    }

    @Override
    public void leave(Player p) {
        players.remove(p);
        aEventos.getInstance().getEconomy().depositPlayer(p, cost);
    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title")).replace("@winner", p.getName())));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        has_winner = true;
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = config.getStringList("Rewards.Commands");
        for(String s : commands) {
            aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), s.replace("@winner", p.getName()));
        }

    }

    @Override
    public void parseMessage(String s, int calls) {
        s = IridiumColorAPI.process(s.replace("&", "§").replace("@broadcasts", String.valueOf(calls)).replace("@name", config.getString("Evento.Title")).replace("@cost", NumberFormatter.parse(this.cost)).replace("@players", String.valueOf(players.size())));
        aEventos.getInstance().getServer().broadcastMessage(s);
    }

    @Override
    public void parseCommand(Player p, String[] args) {

        if(players.contains(p)) {
            p.sendMessage(IridiumColorAPI.process(config.getString("Messages.Already joined").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
            return;
        }

        if(aEventos.getInstance().getEconomy().getBalance(p) < this.cost) {
            p.sendMessage(IridiumColorAPI.process(config.getString("Messages.No money").replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@cost", NumberFormatter.parse(this.cost))));
            return;
        }

        aEventos.getInstance().getEconomy().withdrawPlayer(p, this.cost);

        players.add(p);
        p.sendMessage(IridiumColorAPI.process(config.getString("Messages.Joined").replace("&", "§").replace("@name", config.getString("Evento.Title"))));

    }

    @Override
    public List<Player> getPlayers() { return this.players; }

}
