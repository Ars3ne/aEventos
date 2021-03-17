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
import com.ars3ne.eventos.eventos.Sign;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignListener implements Listener {

    private final Map<Player, Location> checkpoints = new HashMap<>();
    private Sign evento;

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;

        final Block block = e.getClickedBlock();
        if (block.getType().name().contains("SIGN")) {

            org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();

            // Se for a placa da vitória, então chame o evento de vitória com o jogador.
            List<String> victory = evento.getConfig().getStringList("Messages.Sign");

            if(sign.getLine(0).equals(victory.get(0).replace("&", "§"))) {

                for(int i = 1; i < 4; i++) {
                    if(!sign.getLine(i).equals(victory.get(i).replace("&", "§"))) return;
                }

                evento.winner(e.getPlayer());
                return;

            }

            // Se a placa for de checkpoint, então salve o checkpoint.
            if(evento.notReturnOnDamage()) return;
            List<String> checkpoint = evento.getConfig().getStringList("Messages.Checkpoint");

            if(sign.getLine(0).equals(checkpoint.get(0).replace("&", "§"))) {

                for(int i = 1; i < 4; i++) {
                    if(!sign.getLine(i).equals(checkpoint.get(i).replace("&", "§"))) return;
                }

                // Salve o checkpoint.
                checkpoints.put(e.getPlayer(), e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage(evento.getConfig().getString("Messages.Checkpoint saved").replace("&", "§").replace("@name", evento.getConfig().getString("Evento.Title")));

            }

        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e) {

        // Se a entidade não for um jogador, não for dano de queda, ou o mesmo não está no evento, retorne.
        if(e.getEntityType() != EntityType.PLAYER) return;
        Player p = (Player) e.getEntity();

        if(evento == null) return;
        if (!evento.getPlayers().contains(p)) return;
        if(evento.notReturnOnDamage()) return;

        e.setDamage(0);

        // Se o usuário pegou um checkpoint, mande-o para lá.
        if(checkpoints.containsKey(p)) {
            Location check = checkpoints.get(p);
            check.setYaw(p.getLocation().getYaw());
            check.setPitch(p.getLocation().getPitch());
            p.teleport(check);
            p.sendMessage(evento.getConfig().getString("Messages.Checkpoint back").replace("&", "§").replace("@name", evento.getConfig().getString("Evento.Title")));
        }

        // Se não, mande-o para a entrada.
        else {
            YamlConfiguration config = evento.getConfig();
            World w = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Entrance.world"));
            double x = config.getDouble("Locations.Entrance.x");
            double y = config.getDouble("Locations.Entrance.y");
            double z = config.getDouble("Locations.Entrance.z");
            float yaw = p.getLocation().getYaw();
            float pitch = p.getLocation().getPitch();
            p.teleport(new Location(w, x, y, z, yaw, pitch));
            p.sendMessage(evento.getConfig().getString("Messages.Back").replace("&", "§").replace("@name", evento.getConfig().getString("Evento.Title")));
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
        evento = (Sign) aEventos.getEventoManager().getEvento();
    }

}
