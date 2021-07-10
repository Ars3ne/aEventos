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

package com.ars3ne.eventos.listeners.eventos;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.events.PlayerLoseEvent;
import com.ars3ne.eventos.eventos.Fall;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class FallListener implements Listener {

    Fall evento;

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if(evento == null) return;
        if(!evento.getPlayers().contains(e.getEntity())) return;
        e.getEntity().sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§")));
        evento.remove(e.getEntity());
        evento.leaveBungeecord(e.getEntity());
        PlayerLoseEvent lose = new PlayerLoseEvent(e.getEntity(), evento.getConfig().getString("filename").substring(0, evento.getConfig().getString("filename").length() - 4), evento.getType());
        Bukkit.getPluginManager().callEvent(lose);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;
        e.setCancelled(true);
    }

    public void setEvento() {
        evento = (Fall) aEventos.getEventoManager().getEvento();
    }

}
