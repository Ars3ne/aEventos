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
import com.ars3ne.eventos.eventos.BatataQuente;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class BatataQuenteListener implements Listener {

    BatataQuente evento;

    @EventHandler
    public void onPotatoHit(EntityDamageByEntityEvent e) {

        if(evento == null) return;
        if(!(e.getEntity() instanceof Player && e.getDamager() instanceof Player)) return;
        Player damaged = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        if (!evento.getPlayers().contains(damager) || !evento.getPlayers().contains(damaged)) return;
        if(evento.getPotatoHolder() != damager) return;
        if(damager.getItemInHand().getType() != XMaterial.POTATO.parseMaterial()) return;

        // Mude o holder da batata e limpe o inventário do anterior.
        evento.setHolder(damaged, evento.getPotatoHolderChanges());
        damager.getInventory().clear();
        damager.getInventory().setHelmet(null);
        e.setCancelled(true);

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
        evento = (BatataQuente) aEventos.getEventoManager().getEvento();
    }

}
