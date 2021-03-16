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

package com.ars3ne.eventos.eventos;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.Evento;
import com.ars3ne.eventos.api.events.PlayerLoseEvent;
import com.ars3ne.eventos.listeners.eventos.SpleefListener;
import com.ars3ne.eventos.utils.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class Spleef extends Evento {

    private final YamlConfiguration config;
    private final SpleefListener listener = new SpleefListener();

    private BukkitTask runnable;
    private final BukkitScheduler scheduler = aEventos.getInstance().getServer().getScheduler();

    private final Cuboid cuboid;
    private final int delay;
    private final int inactive_time;
    private boolean can_break;
    private final boolean kick_inactive;

    public Spleef(YamlConfiguration config) {
        super(config);

        this.config = config;
        can_break = false;
        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());

        // Obtenha o cuboid
        World world = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Pos1.world"));
        Location pos1 = new Location(world, config.getDouble("Locations.Pos1.x"), config.getDouble("Locations.Pos1.y"), config.getDouble("Locations.Pos1.z"));
        Location pos2 = new Location(world, config.getDouble("Locations.Pos2.x"), config.getDouble("Locations.Pos2.y"), config.getDouble("Locations.Pos2.z"));
        cuboid = new Cuboid(pos1, pos2);

        // Encha o cuboid de neve.
        for (Block b : cuboid.getBlocks()) {
            b.setType(Material.SNOW_BLOCK);
        }

        delay = config.getInt("Evento.Delay"); // Obtenha o delay.
        inactive_time = config.getInt("Evento.Kick time"); // Obtenha o tempo de expulsar um jogador.
        kick_inactive = config.getBoolean("Evento.Kick");

    }

    @SuppressWarnings("deprecation")
    @Override
    public void start() {

        listener.setEvento();

        // De os itens para os jogadores.
        for(String s: config.getStringList("Items")) {

            String[] separated = s.split("-");

            for(Player p: getPlayers()) {
                p.getInventory().addItem(new ItemStack(Material.getMaterial(Integer.parseInt(separated[0])), Integer.parseInt(separated[1])));
            }

        }

        // Envie a mensagem para todos os usuários no evento.
        List<String> starting_level = config.getStringList("Messages.Enabling breaking");
        for (Player player : getPlayers()) {
            for(String s : starting_level) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(delay)).replace("@name", config.getString("Evento.Title")));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : starting_level) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(delay)).replace("@name", config.getString("Evento.Title")));
            }
        }

        // Aguarde o tempo na config para permitir a quebra de blocos.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            this.allowBreakBlocks(); // Permita a quebra de blocos.

            // Envie a mensagem para todos os usuários no evento.
            List<String> break_blocks = config.getStringList("Messages.Breaking allowed");
            for (Player player : getPlayers()) {
                for(String s : break_blocks) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : break_blocks) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }


        }, delay * 20L);

        if(kick_inactive) {

            // Verifique se os jogadores estão inativos.
            runnable = scheduler.runTaskTimer(aEventos.getInstance(), () -> {

                if(!isHappening()) runnable.cancel();
                if(canNotBreakBlocks()) return;
                checkInactive();

            }, 0, 20L);

        }

    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title")));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = config.getStringList("Rewards.Commands");
        for(String s : commands) {
            aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), s.replace("@winner", p.getName()));
        }

    }

    @Override
    public void stop() {

        // Encha o cuboid de neve.
        for (Block b : cuboid.getBlocks()) {
            b.setType(Material.SNOW_BLOCK);
        }

        // Limpe o inventário de todos os jogadores do evento.
        for(Player p: getPlayers()) {
            p.getInventory().clear();
            listener.getLastMove().remove(p);
        }

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    private void checkInactive() {

        if(!isHappening()) return;
        long time = System.currentTimeMillis();

        for(Player p: listener.getLastMove().keySet()) {

            if(!listener.getLastMove().containsKey(p)) continue;
            if(!getPlayers().contains(p)) {
                listener.getLastMove().remove(p);
                continue;
            }


            // Se o jogador está parado a mais tempo do que o limite, elimine-o do evento.
            if(time >= (listener.getLastMove().get(p) + (inactive_time * 1000L))) {
                p.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
                remove(p);
                PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
                Bukkit.getPluginManager().callEvent(lose);
                listener.getLastMove().remove(p);
                continue;
            }

            // Se o jogador está parado á metade do tempo limite, mande um aviso.
            if(time >= (listener.getLastMove().get(p) + (inactive_time / 2) * 1000L)) {
                p.sendMessage(config.getString("Messages.Kick").replace("&", "§").replace("@time", String.valueOf( Math.abs((time - (listener.getLastMove().get(p) + (inactive_time+1) * 1000L)) / 1000L ))).replace("@name", config.getString("Evento.Title")));
            }

        }
    }

    public boolean canNotBreakBlocks() {
        return !this.can_break;
    }
    public void allowBreakBlocks() {
        this.can_break = true;
    }

}
