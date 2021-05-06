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
import com.ars3ne.eventos.eventos.Hunter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class HunterListener implements Listener {

    Hunter evento;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        if(evento == null) return;
        if(!(e.getEntity() instanceof Player)) return;
        if(!(e.getDamager() instanceof Arrow)) return;

        Player p = (Player) e.getEntity();
        Arrow arrow = (Arrow) e.getDamager();
        Player shooter = (Player) arrow.getShooter();

        if(!evento.getPlayers().contains(p) || !evento.getPlayers().contains(shooter)) return;

        if(!evento.isPvPEnabled()) {
            e.setCancelled(true);
            return;
        }

        if(evento.getCaptured().contains(p) || evento.getCaptured().contains(shooter)) return;

        if((evento.getBlueTeam().containsKey(p) && evento.getBlueTeam().containsKey(shooter))
                || (evento.getRedTeam().containsKey(p) && evento.getRedTeam().containsKey(shooter))) {
            e.setCancelled(true);
            return;
        }


        evento.eliminate(p, shooter);

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getWhoClicked())) return;
        e.setCancelled(true);
    }

    public void setEvento() {
        evento = (Hunter) aEventos.getEventoManager().getEvento();
    }
}
