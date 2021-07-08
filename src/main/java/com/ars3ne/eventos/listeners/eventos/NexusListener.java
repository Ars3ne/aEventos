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
import com.ars3ne.eventos.eventos.Nexus;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.List;

public class NexusListener implements Listener {

    Nexus evento;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        if(e.getEntity() instanceof EnderCrystal) {

            // Se o dano foi para um EnderCrystal, então verifique se o mesmo é um nexus.
            if(!(e.getDamager() instanceof Player)) return;

            EnderCrystal crystal = (EnderCrystal) e.getEntity();
            Player player = (Player) e.getDamager();

            if(!crystal.hasMetadata("Nexus")) return;

            e.setCancelled(true);
            if(evento == null) return;
            if(!evento.isPvPEnabled()) return;
            if(!evento.getPlayers().contains(player)) return;

            // Se o nexus possuir o metadata "Blue", significa que é do time Azul.
            if(crystal.hasMetadata("Blue")) {

                // Se um jogador está batendo no nexus do seu time, retorne.
                if(evento.getBlueTeam().containsKey(player)) {
                    List<String> same_hit = evento.getConfig().getStringList("Messages.Same hit");
                    for(String s: same_hit) {
                        player.sendMessage(s.replace("&", "§").replace("@name", evento.getConfig().getString("Evento.Title")));
                    }
                    return;
                }

                // Dê dano ao nexus.
                int new_health = evento.getBlueNexusHealth() - evento.getNexusDamage();

                // Se a vida do nexus for negativa, então dê a vitória para o time adversário.
                if(new_health <= 0) {
                    evento.win("red");
                }else {
                    // Se não, então atualize o nome e a vida do nexus.
                    evento.setBlueNexusHealth(new_health);
                    crystal.setCustomName(evento.getNexusName().replace("&", "§").replace("@team_color", "§9").replace("@team_uppercase", evento.getBlueTeamName().toUpperCase()).replace("@team", evento.getBlueTeamName()).replace("@health", String.valueOf(new_health)));
                }

            }else {

                // Se um jogador está batendo no nexus do seu time, retorne.
                if(evento.getRedTeam().containsKey(player)) {
                    List<String> same_hit = evento.getConfig().getStringList("Messages.Same hit");
                    for(String s: same_hit) {
                        player.sendMessage(s.replace("&", "§").replace("@name", evento.getConfig().getString("Evento.Title")));
                    }
                    return;
                }

                // Dê dano ao nexus.
                int new_health = evento.getRedNexusHealth() - evento.getNexusDamage();

                // Se a vida do nexus for negativa, então dê a vitória para o time adversário.
                if(new_health <= 0) {
                    evento.win("blue");
                }else {
                    // Se não, então atualize o nome e a vida do nexus.
                    evento.setRedNexusHealth(new_health);
                    crystal.setCustomName(evento.getNexusName().replace("&", "§").replace("@team_color", "§c").replace("@team_uppercase", evento.getRedTeamName().toUpperCase()).replace("@team", evento.getRedTeamName()).replace("@health", String.valueOf(new_health)));
                }

            }

        }else if((e.getEntity() instanceof Player)) {

            // Se o dano foi para um jogador, faça as verificações necessárias.
            if(evento == null) return;
            if(!(e.getDamager() instanceof Player)) return;

            Player damaged = (Player) e.getEntity();
            Player damager = (Player) e.getDamager();

            if(!evento.getPlayers().contains(damaged) || !evento.getPlayers().contains(damager)) return;

            if(evento.getSpectators().contains(damaged) || evento.getSpectators().contains(damager)) {
                e.setCancelled(true);
                return;
            }

            if(evento.getDeadPlayers().contains(damaged) || evento.getDeadPlayers().contains(damager)) {
                e.setCancelled(true);
                return;
            }

            if(evento.getInvinciblePlayers().contains(damaged) || evento.getInvinciblePlayers().contains(damager)) {
                e.setCancelled(true);
                return;
            }

            if(!evento.isPvPEnabled()) {
                e.setCancelled(true);
                return;
            }

            if((evento.getBlueTeam().containsKey(damaged) && evento.getBlueTeam().containsKey(damager))
                    || (evento.getRedTeam().containsKey(damaged) && evento.getRedTeam().containsKey(damager))) {
                e.setCancelled(true);
            }

        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent e) {

        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getEntity()) || !evento.getPlayers().contains(e.getEntity().getKiller())) return;

        // Limpe os drops e defina o jogador como o perdedor.
        e.getDrops().clear();
        e.setKeepLevel(true);
        evento.eliminate(e.getEntity().getPlayer());
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
        if (!evento.getPlayers().contains((Player) e.getWhoClicked())) return;
        e.setCancelled(true);
    }

    public void setEvento() {
        evento = (Nexus) aEventos.getEventoManager().getEvento();
    }
}
