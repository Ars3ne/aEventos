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
import com.ars3ne.eventos.eventos.Semaforo;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.List;

public class SemaforoListener implements Listener {

    Semaforo evento;
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;

        // Se o bloco for uma placa, então verifique se é a placa de vitória.
        final Block block = e.getClickedBlock();
        if (block.getType().name().contains("SIGN")) {

            Sign sign = (Sign) block.getState();

            // Se for a placa da vitória, então chame o evento de vitória com o jogador.
            List<String> victory = evento.getConfig().getStringList("Messages.Sign");

            if(sign.getLine(0).equals(victory.get(0).replace("&", "§"))) {

                for(int i = 1; i < 4; i++) {
                    if(!sign.getLine(i).equals(victory.get(i).replace("&", "§"))) return;
                }
                evento.winner(e.getPlayer());

            }

        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;
        if(evento.canWalk()) return;

        // Se o jogador não se moveu, retorne.
        if((e.getFrom().getX() == e.getTo().getX()) && (e.getFrom().getZ() == e.getTo().getZ())) return;

        // Remova o jogador do evento.
        e.getPlayer().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
        evento.remove(e.getPlayer());
        PlayerLoseEvent lose = new PlayerLoseEvent(e.getPlayer(), evento.getConfig().getString("filename").substring(0, evento.getConfig().getString("filename").length() - 4), evento.getType());
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

    @SuppressWarnings("SuspiciousMethodCalls")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getWhoClicked())) return;
        e.setCancelled(true);
    }

    public void setEvento() {
        evento = (Semaforo) aEventos.getEventoManager().getEvento();
    }

}
