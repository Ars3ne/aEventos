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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class Loteria extends EventoChat {

    private final YamlConfiguration config;

    private final int number, max_number;
    private final long reward, cost;

    public Loteria(YamlConfiguration config) {

        super(config);

        this.config = config;
        this.cost = config.getInt("Evento.Cost");
        this.max_number = config.getInt("Evento.Max number");
        this.reward = config.getInt("Evento.Reward");

        this.number = (int) (Math.floor(Math.random() * this.max_number ) + 1);

    }

    @Override
    public void start() {

        // Pare o evento sem vencedores.
        List<String> broadcast_messages = config.getStringList("Messages.No winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@number", String.valueOf(this.number)));
        }

        stop();

    }

    @Override
    public void stop() {
        removePlayers();
    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title")).replace("@number", String.valueOf(this.number)));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = config.getStringList("Rewards.Commands");
        for(String s : commands) {
            aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), s.replace("@winner", p.getName()));
        }

        // Deposite o valor na conta do vencedor.
        aEventos.getInstance().getEconomy().depositPlayer(p, this.reward);

    }

    @Override
    public void parseMessage(String s, int calls) {
        s = s.replace("&", "§").replace("@broadcasts", String.valueOf(calls)).replace("@name", config.getString("Evento.Title")).replace("@reward", aEventos.getInstance().getEconomy().format(this.reward)).replace("@cost", aEventos.getInstance().getEconomy().format(this.cost)).replace("@max", String.valueOf(this.max_number));
        aEventos.getInstance().getServer().broadcastMessage(s);
    }

    @Override
    public void parseCommand(Player p, String[] args) {

        if(aEventos.getInstance().getEconomy().getBalance(p) < this.cost) {
            p.sendMessage(config.getString("Messages.No money").replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@cost", aEventos.getInstance().getEconomy().format(this.cost)));
            return;
        }

        try{

            int player_number = Integer.parseInt(args[0]);

            if(player_number <= 0 || player_number > this.max_number) {
                p.sendMessage(config.getString("Messages.Invalid").replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@max", String.valueOf(this.max_number)));
                return;
            }

            aEventos.getInstance().getEconomy().withdrawPlayer(p, this.cost);

            if(player_number == this.number) {
                winner(p);
            }else {
                p.sendMessage(config.getString("Messages.Lose").replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@number", String.valueOf(player_number)));
            }

        }catch(NumberFormatException e) {
            p.sendMessage(config.getString("Messages.Invalid").replace("&", "§").replace("@name", config.getString("Evento.Title")).replace("@max", String.valueOf(this.max_number)));
        }

    }

}
