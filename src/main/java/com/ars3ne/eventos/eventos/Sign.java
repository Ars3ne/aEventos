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
import com.ars3ne.eventos.listeners.eventos.SignListener;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;

public class Sign extends Evento {

    private final YamlConfiguration config;
    private final SignListener listener = new SignListener();
    private final boolean return_on_damage;

    public Sign(YamlConfiguration config) {
        super(config);
        this.config = config;
        this.return_on_damage = config.getBoolean("Evento.Return on damage");

    }

    @Override
    public void start() {
        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();
    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", getConfig().getString("Evento.Title"))));
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
        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    public boolean notReturnOnDamage() { return !this.return_on_damage; }

}
