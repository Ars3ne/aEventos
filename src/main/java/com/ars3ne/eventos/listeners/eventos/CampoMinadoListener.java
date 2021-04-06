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
import com.ars3ne.eventos.eventos.CampoMinado;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class CampoMinadoListener implements Listener {

    CampoMinado evento;

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if(evento == null) return;
        if(!evento.getPlayers().contains(e.getPlayer())) return;

        if(e.getTo().getBlock().getType() == Material.WATER || (!XMaterial.isNewVersion() && e.getTo().getBlock().getType() == Material.STATIONARY_WATER)) {

            // Se o jogador entrou na água, então o elimine.
            e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
            evento.remove(e.getPlayer());
            evento.notifyLeave(e.getPlayer());
            PlayerLoseEvent lose = new PlayerLoseEvent(e.getPlayer(), evento.getConfig().getString("filename").substring(0, evento.getConfig().getString("filename").length() - 4), evento.getType());
            Bukkit.getPluginManager().callEvent(lose);

        }

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
        evento = (CampoMinado) aEventos.getEventoManager().getEvento();
    }
}
