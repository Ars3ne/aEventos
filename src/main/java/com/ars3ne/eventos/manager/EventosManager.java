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

package com.ars3ne.eventos.manager;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.Evento;
import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.eventos.*;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class EventosManager {

    private Evento evento = null;

    public boolean startEvento(EventoType type, YamlConfiguration config) {

        if(config == null) {
            this.evento = null;
            return false;
        }

        if(!verify(config)) return false;
        if(evento != null) return false;

        switch(type) {
            case SIGN:
                this.evento = new Sign(config);
                break;
            case CAMPO_MINADO:
                this.evento = new CampoMinado(config);
                break;
            case SPLEEF:
                this.evento = new Spleef(config);
                break;
            case SEMAFORO:
                this.evento = new Semaforo(config);
                break;
            case BATATA_QUENTE:
                this.evento = new BatataQuente(config);
                break;
            case FROG:
                this.evento = new Frog(config);
                break;
            case FIGHT:
                this.evento = new Fight(config);
                break;
            case KILLER:
                this.evento = new Killer(config);
                break;
            case SUMO:
                this.evento = new Sumo(config);
                break;
            case FALL:
                this.evento = new Fall(config);
                break;
            case PAINTBALL:
                this.evento = new Paintball(config);
                break;
            case HUNTER:
                this.evento = new Hunter(config);
                break;
            case QUIZ:
                this.evento = new Quiz(config);
                break;
            case ANVIL:
                this.evento = new Anvil(config);
                break;
            case GUERRA:
                this.evento = new Guerra(config);
                break;
            case NEXUS:
                this.evento = new Nexus(config);
                break;
            case THOR:
                this.evento = new Thor(config);
                break;
        }

        assert this.evento != null;
        this.evento.startCall();
        return true;

    }

    public boolean startEvento(EventoType type, YamlConfiguration config, double reward) {
        if(reward != -1) config.set("custom_reward", reward);
        return startEvento(type, config);
    }

    private boolean verify(YamlConfiguration config) {

        boolean require_pos = false;

        switch(EventoType.getEventoType(config.getString("Evento.Type"))) {
            case CAMPO_MINADO: case SPLEEF: case FROG: case FIGHT: case PAINTBALL: case HUNTER: case QUIZ: case ANVIL: case NEXUS:
                require_pos = true;
                break;
        }

        switch(EventoType.getEventoType(config.getString("Evento.Type"))) {
            case BATATA_QUENTE: case SEMAFORO: case PAINTBALL: case HUNTER:

                if(!config.getBoolean("Evento.Empty inventory")) {

                    Bukkit.getConsoleSender().sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Require empty inventory")));

                    for(Player p: Bukkit.getOnlinePlayers()) {
                        if(!p.hasPermission("aeventos.admin")) continue;
                        p.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Require empty inventory")));
                    }

                    return false;

                }
                break;
        }

        if(config.isSet("Itens.Enabled")) {

            if(config.getBoolean("Itens.Enabled") && !config.getBoolean("Evento.Empty inventory")) {

                Bukkit.getConsoleSender().sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Require empty inventory")));

                for(Player p: Bukkit.getOnlinePlayers()) {
                    if(!p.hasPermission("aeventos.admin")) continue;
                    p.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Require empty inventory")));
                }

                return false;
            }

        }

        // Se alguma das localizações não está definida, cancele o evento e mande uma mensagem para o console.
        return config.getConfigurationSection("Locations.Lobby") != null &&
                config.getConfigurationSection("Locations.Entrance") != null &&
                (!config.getBoolean("Evento.Spectator mode") || config.getConfigurationSection("Locations.Spectator") != null) &&
                config.getConfigurationSection("Locations.Exit") != null
                && (!require_pos || (config.getConfigurationSection("Locations.Pos1") != null && config.getConfigurationSection("Locations.Pos2") != null));

    }

    public Evento getEvento() {
        return this.evento;
    }

}
