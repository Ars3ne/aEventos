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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FastClick extends EventoChat {

    private final YamlConfiguration config;

    private final String clickable;
    private final String not_clickable;
    private double reward = getReward();
    private final int total_lines;

    private final int correct_line;
    private final int correct_index;

    public FastClick(YamlConfiguration config) {

        super(config);

        this.config = config;
        if(this.reward == -1) this.reward = config.getDouble("Evento.Reward");
        this.clickable = config.getString("Messages.Clickable");
        this.not_clickable = config.getString("Messages.Not clickable");
        this.total_lines = config.getInt("Evento.Lines");

        this.correct_line = ThreadLocalRandom.current().nextInt(1, config.getInt("Evento.Lines") + 1);
        this.correct_index = ThreadLocalRandom.current().nextInt(1, config.getInt("Evento.Quantity") + 1);

    }

    @Override
    public void start() {

        // Pare o evento sem vencedores.
        List<String> broadcast_messages = config.getStringList("Messages.No winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
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
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title")).replace("@winner", p.getName())));
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

        if(!this.isHappening()) {
            this.stop();
            return;
        }

        s = IridiumColorAPI.process(s.replace("&", "§").replace("@broadcasts", String.valueOf(calls)).replace("@name", config.getString("Evento.Title")).replace("@reward", NumberFormatter.parse(this.reward)));

        for(int i = 1; i <= total_lines; i++) {

            if(s.contains("@line" + i)) {

                s = s.replace("@line" + i, "");

                TextComponent component = new TextComponent(TextComponent.fromLegacyText(IridiumColorAPI.process(ChatColor.translateAlternateColorCodes('&', s))));

                for(int z = 1; z <= config.getInt("Evento.Quantity"); z++) {

                    String p = "";

                    if(i == correct_line && z == correct_index) {
                        p += clickable;
                    }else {
                        p += not_clickable;
                    }

                    TextComponent click = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', p)));

                    click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/evento " + i + " " + z));

                    component.addExtra(click);
                }

                if(this.isHappening()) aEventos.getInstance().getServer().spigot().broadcast(component);
                return;

            }

        }

        if(this.isHappening()) aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s));

    }

    @Override
    public void parseCommand(Player p, String[] args) {

        if(!this.isHappening()) {
            this.stop();
            return;
        }

        try {

            int line = Integer.parseInt(args[0]);
            int index = Integer.parseInt(args[1]);

            if(line == correct_line && index == correct_index) {
                winner(p);
            }else {
                p.sendMessage(IridiumColorAPI.process(config.getString("Messages.Wrong").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
            }

        }catch(NumberFormatException ignored) {
            p.sendMessage(IridiumColorAPI.process(config.getString("Messages.Wrong").replace("&", "§").replace("@name", config.getString("Evento.Title"))));
        }
    }
}
