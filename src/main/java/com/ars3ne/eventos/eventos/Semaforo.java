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
import com.ars3ne.eventos.listeners.eventos.SemaforoListener;
import com.ars3ne.eventos.utils.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class Semaforo extends Evento {

    private final YamlConfiguration config;
    private final SemaforoListener listener = new SemaforoListener();

    private final boolean hotbar;
    private boolean semaforo_happening;
    private boolean can_walk;
    private final int green;
    private final int yellow;
    private final int red;
    private final String green_name;
    private final String yellow_name;
    private final String red_name;

    private BukkitTask runnable;
    private final BukkitScheduler scheduler = aEventos.getInstance().getServer().getScheduler();

    public Semaforo(YamlConfiguration config) {

        super(config);

        this.config = config;
        this.can_walk = false;
        this.hotbar = config.getBoolean("Evento.Show on hotbar");

        this.green = config.getInt("Evento.Green");
        this.yellow = config.getInt("Evento.Yellow");
        this.red = config.getInt("Evento.Red");

        this.green_name = config.getString("Messages.Green item").replace("&", "§");
        this.yellow_name = config.getString("Messages.Yellow item").replace("&", "§");
        this.red_name = config.getString("Messages.Red item").replace("&", "§");

    }

    @Override
    public void start() {
        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        runnable = scheduler.runTaskTimer(aEventos.getInstance(), () -> {

            if(!isHappening()) runnable.cancel();
            if(!semaforo_happening) {
                semaforo();
            }

        }, 0, 20L);

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
        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    private void semaforo() {

        if(!isHappening()) return;

        this.semaforo_happening = true;

        // Cor verde
        allowWalking();

        // Mande a mensagem para todos os jogadores.
        List<String> green_st = config.getStringList("Messages.Green");
        for (Player player : getPlayers()) {
            for(String s : green_st) {
                player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : green_st) {
                player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
            }
        }

        // Se a hotbar estiver ativada, limpe o inventário e coloque os blocos.
        if(this.hotbar && requireEmptyInventory()) {

            ItemStack item = XMaterial.LIME_TERRACOTTA.parseItem();

            assert item != null;
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(this.green_name);
            item.setItemMeta(meta);

            for(Player player: getPlayers()) {
                player.getInventory().clear();
                for(int i = 0; i < 9; i++) {
                    player.getInventory().setItem(i, item);
                }
            }

        }


        // Cor amarela
        aEventos.getInstance().getServer().getScheduler().runTaskLater(aEventos.getInstance(), () -> {

            if(!isHappening()) return;

            // Mande a mensagem para todos os jogadores.
            List<String> yellow_st = config.getStringList("Messages.Yellow");
            for (Player player : getPlayers()) {
                for(String s : yellow_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : yellow_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }

            // Se a hotbar estiver ativada, limpe o inventário e coloque os blocos.
            if(this.hotbar && requireEmptyInventory()) {

                ItemStack item = XMaterial.YELLOW_TERRACOTTA.parseItem();

                assert item != null;
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(this.yellow_name);
                item.setItemMeta(meta);

                for(Player player: getPlayers()) {

                    player.getInventory().clear();
                    for(int i = 0; i < 9; i++) {
                        player.getInventory().setItem(i, item);
                    }
                }

            }

        }, green * 20L);

        // Cor vermelha
        aEventos.getInstance().getServer().getScheduler().runTaskLater(aEventos.getInstance(), () -> {

            if(!isHappening()) return;
            denyWalking();

            // Mande a mensagem para todos os jogadores.
            List<String> red_st = config.getStringList("Messages.Red");
            for (Player player : getPlayers()) {
                for(String s : red_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : red_st) {
                    player.sendMessage(s.replace("&", "§").replace("@name", config.getString("Evento.Title")));
                }
            }

            // Se a hotbar estiver ativada, limpe o inventário e coloque os blocos.
            if(this.hotbar && requireEmptyInventory()) {

                ItemStack item = XMaterial.RED_TERRACOTTA.parseItem();

                assert item != null;
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(this.red_name);
                item.setItemMeta(meta);

                for(Player player: getPlayers()) {
                    player.getInventory().clear();
                    for(int i = 0; i < 9; i++) {
                        player.getInventory().setItem(i, item);
                    }
                }
            }

        }, (green + yellow) * 20L);

        // Finalize o ciclo.
        aEventos.getInstance().getServer().getScheduler().runTaskLater(aEventos.getInstance(), () -> this.semaforo_happening = false, (green + yellow + red) * 20L);

    }

    public boolean canWalk() { return this.can_walk; }

    public void allowWalking() {
        this.can_walk = true;
    }

    public void denyWalking() {
        this.can_walk = false;
    }

}
