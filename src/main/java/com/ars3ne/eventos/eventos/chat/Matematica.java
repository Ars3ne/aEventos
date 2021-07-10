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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Matematica extends EventoChat {

    private final YamlConfiguration config;

    private String sum;
    private int result;

    private final long reward;

    public Matematica(YamlConfiguration config) {

        super(config);

        this.config = config;
        this.reward = config.getLong("Evento.Reward");

        int account_type = ThreadLocalRandom.current().nextInt(0, 1 + 1);

        int number1 = ThreadLocalRandom.current().nextInt(config.getInt("Evento.Min"), config.getInt("Evento.Max") + 1);
        int number2 = ThreadLocalRandom.current().nextInt(config.getInt("Evento.Min"), config.getInt("Evento.Max") + 1);

        switch(account_type) {
            case 0: // Adição
                sum = number1  + " + " + number2;
                result = Math.round(number1 + number2);
                break;
            case 1: // Subtração
                sum = number1  + " - " + number2;
                result = Math.round(number1 - number2);
                break;
        }

    }

    @Override
    public void start() {

        // Pare o evento sem vencedores.
        List<String> broadcast_messages = config.getStringList("Messages.No winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@result", String.valueOf(result)).replace("@name", config.getString("Evento.Title"))));
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
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", p.getName()).replace("@result", String.valueOf(result)).replace("@name", config.getString("Evento.Title")).replace("@winner", p.getName())));
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
        s = IridiumColorAPI.process(s.replace("&", "§").replace("@broadcasts", String.valueOf(calls)).replace("@sum", sum).replace("@name", config.getString("Evento.Title")).replace("@reward", NumberFormatter.parse(this.reward)));
        aEventos.getInstance().getServer().broadcastMessage(s);
    }

    @Override
    public void parsePlayerMessage(Player p, String message) {

        try{

            int integer = Integer.parseInt(message);

            if(integer == result) {
                winner(p);
            }

        }catch(NumberFormatException ignored) {}

    }


}
