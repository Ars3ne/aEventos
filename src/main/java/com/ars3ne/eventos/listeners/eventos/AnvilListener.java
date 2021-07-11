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
import com.ars3ne.eventos.eventos.Anvil;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.ArrayList;
import java.util.List;

public class AnvilListener implements Listener {

    private Anvil evento;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent e) {

        if(evento == null) return;
        if(e.getEntityType() != EntityType.FALLING_BLOCK) return;

        FallingBlock fb = (FallingBlock) e.getEntity();
        if(fb.getMaterial() != Material.ANVIL) return;
        if(!evento.getAnvils().contains(e.getBlock())) return;

        fb.setDropItem(false);
        fb.remove();
        if(e.getBlock().getType() != Material.ANVIL) e.getBlock().setType(Material.ANVIL);

        List<Player> eliminate = new ArrayList<>();
        for(Player p: evento.getPlayers()) {

            // Se o jogador estiver no mesmo bloco da bigorna, então o elimine.
            if(Math.floor(p.getLocation().getX()) != e.getBlock().getX() || Math.floor(p.getLocation().getY()) != e.getBlock().getY() || Math.floor(p.getLocation().getZ()) != e.getBlock().getZ() ) continue;
            eliminate.add(p);

        }

        for(Player p: eliminate) {
            p.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§")));
            evento.remove(p);
            PlayerLoseEvent lose = new PlayerLoseEvent(p, evento.getConfig().getString("filename").substring(0, evento.getConfig().getString("filename").length() - 4), evento.getType());
            Bukkit.getPluginManager().callEvent(lose);
        }
        eliminate.clear();

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void EntityChangeBlock(EntityChangeBlockEvent e) {
        if (e.getEntityType() == EntityType.FALLING_BLOCK && evento.getAnvils().contains(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    public void setEvento() {
        evento = (Anvil) aEventos.getEventoManager().getEvento();
    }

}
