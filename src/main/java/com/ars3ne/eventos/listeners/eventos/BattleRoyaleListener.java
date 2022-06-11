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
import com.ars3ne.eventos.eventos.BattleRoyale;
import com.cryptomorin.xseries.XMaterial;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class BattleRoyaleListener implements Listener {

    private BattleRoyale evento;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {

        if(evento == null) return;

        // Se a entidade não for um player, retorne.
        if(!(e.getEntity() instanceof Player)) return;

        Player damaged = (Player) e.getEntity();

        if(!evento.getPlayers().contains(damaged)) return;
        if(!evento.isPvPEnabled()) e.setCancelled(true);

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getEntity())) return;

        // Remova o jogador do evento.
        e.getEntity().sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§")));
        evento.remove(e.getEntity());
        evento.leaveBungeecord(e.getEntity());
        PlayerLoseEvent lose = new PlayerLoseEvent(e.getEntity(), evento.getConfig().getString("filename").substring(0, evento.getConfig().getString("filename").length() - 4), evento.getType());
        Bukkit.getPluginManager().callEvent(lose);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if(e.isCancelled()) return;

        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;
        if(!evento.removePlayerPlacedBlocks()) return;

        evento.getBlocksToRemove().add(e.getBlockPlaced());

    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {

        if(e.isCancelled()) return;

        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;
        if(!evento.removePlayerPlacedBlocks()) return;

        if(!evento.getBlocksToRemove().contains(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if(evento == null) return;
        if(!evento.getPlayers().contains(e.getPlayer())) return;

        if(e.getTo().getBlock().getType() == Material.WATER || (!XMaterial.supports(13) && e.getTo().getBlock().getType() == Material.STATIONARY_WATER)) {

            // Se o jogador entrou na água, então o elimine.
            evento.eliminate(e.getPlayer());

        }

    }

    @EventHandler
    public void onBucketUse(PlayerBucketEmptyEvent e) {

        if(e.isCancelled()) return;

        if(evento == null) return;
        if (!evento.getPlayers().contains(e.getPlayer())) return;
        if(!evento.removePlayerPlacedBlocks()) return;

        evento.getBlocksToRemove().add(e.getBlockClicked().getRelative(e.getBlockFace()));

    }

    @EventHandler
    public void onSpread(BlockFromToEvent e){

        if(!evento.getBlocksToRemove().contains(e.getBlock()) && !evento.getBlocksToRemove().contains(e.getToBlock())) return;

        Block to1 = e.getToBlock().getRelative(BlockFace.EAST);
        Block to2 = e.getToBlock().getRelative(BlockFace.WEST);
        Block to3 = e.getToBlock().getRelative(BlockFace.SOUTH);
        Block to4 = e.getToBlock().getRelative(BlockFace.NORTH);
        Block to5 = e.getToBlock().getRelative(BlockFace.DOWN);

        if(to1.isLiquid()){
            evento.getBlocksToRemove().add(to1);
        }

        if(to2.isLiquid()){
            evento.getBlocksToRemove().add(to2);
        }

        if(to3.isLiquid()) {
            evento.getBlocksToRemove().add(to3);
        }

        if(to4.isLiquid()){
            evento.getBlocksToRemove().add(to4);
        }

        if(to5.isLiquid()){
            evento.getBlocksToRemove().add(to5);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onIgnite(BlockIgniteEvent e) {

        if(e.isCancelled()) return;

        if(evento == null) return;
        if(!evento.removePlayerPlacedBlocks()) return;

        if(e.getPlayer() != null) {
            if (!evento.getPlayers().contains(e.getPlayer())) return;
            if (!evento.getBlocksToRemove().contains(e.getBlock())) {
                e.setCancelled(true);
                return;
            }
        }

        if(evento.getBlocksToRemove().contains(e.getIgnitingBlock()) && !evento.getBlocksToRemove().contains(e.getBlock())) e.setCancelled(true);

    }


    public void setEvento() {
        evento = (BattleRoyale) aEventos.getEventoManager().getEvento();
    }

}
