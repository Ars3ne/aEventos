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
import com.ars3ne.eventos.eventos.Fight;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class FightListener implements Listener {

    private Fight evento;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {

        if(evento == null) return;

        // Se a entidade não for um player, retorne.
        if(!(e.getEntity() instanceof Player && e.getDamager() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        if(!evento.getPlayers().contains(damaged) || !evento.getPlayers().contains(damager)) return;
        if((evento.getFighter1() != damaged && evento.getFighter1() != damager) && (evento.getFighter2() != damaged && evento.getFighter2() != damager)) e.setCancelled(true);

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getEntity()) || !evento.getPlayers().contains(e.getEntity().getKiller())) return;
        if((evento.getFighter1() != e.getEntity() && evento.getFighter1() != e.getEntity().getKiller()) && (evento.getFighter2() != e.getEntity() && evento.getFighter2() != e.getEntity().getKiller())) return;

        // Limpe os drops e defina o jogador como o perdedor.
        e.getDrops().clear();
        evento.setFightLoser(e.getEntity().getPlayer());
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
        evento = (Fight) aEventos.getEventoManager().getEvento();
    }

}
