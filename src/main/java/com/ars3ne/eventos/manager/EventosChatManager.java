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
import com.ars3ne.eventos.api.EventoChat;
import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.eventos.chat.*;
import com.ars3ne.eventos.listeners.EventoChatListener;
import org.bukkit.configuration.file.YamlConfiguration;

public class EventosChatManager {

    private EventoChat evento = null;
    private final EventoChatListener listener = new EventoChatListener();

    public boolean startEvento(EventoType type, YamlConfiguration config) {

        if(config == null) {
            this.evento = null;
            return false;
        }

        if(!verify(config)) return false;
        if(evento != null) return false;

        switch(type) {
            case VOTACAO:
                this.evento = new Votacao(config);
                break;
            case LOTERIA:
                this.evento = new Loteria(config);
                break;
            case BOLAO:
                this.evento = new Bolao(config);
                break;
            case MATEMATICA:
                this.evento = new Matematica(config);
                break;
            case PALAVRA:
                this.evento = new Palavra(config);
                break;
            case FAST_CLICK:
                this.evento = new FastClick(config);
                break;
            case SORTEIO:
                this.evento = new Sorteio(config);
                break;
        }

        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        this.evento.startCall();

        return true;

    }

    public boolean startEvento(EventoType type, YamlConfiguration config, double reward) {
        config.set("custom_reward", reward);
        return startEvento(type, config);
    }

    private boolean verify(YamlConfiguration config) {

        switch(EventoType.getEventoType(config.getString("Evento.Type"))) {
            case LOTERIA: case BOLAO: case MATEMATICA: case PALAVRA: case FAST_CLICK:
                return aEventos.getInstance().getEconomy() != null;
        }

        return true;

    }
    public EventoChat getEvento() {
        return this.evento;
    }

}
