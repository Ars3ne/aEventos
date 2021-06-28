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
import com.ars3ne.eventos.listeners.eventos.BatataQuenteListener;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class BatataQuente extends Evento {

    private final YamlConfiguration config;
    private final BatataQuenteListener listener = new BatataQuenteListener();
    private final int max_time;
    private int potato_holder_changes = 0;
    private int task;

    private Player potato_holder;

    final Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
    final Team potato_holder_team = board.registerNewTeam("potato_holder");

    public BatataQuente(YamlConfiguration config) {
        super(config);
        this.config = config;
        
        this.max_time = config.getInt("Evento.Time");
        potato_holder_team.setPrefix(ChatColor.RED.toString());
    }

    @Override
    public void start() {
        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();
        randomHolder();

    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = this.config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title")));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = this.config.getStringList("Rewards.Commands");
        for(String s : commands) {
            executeConsoleCommand(p, s.replace("@winner", p.getName()));
        }

    }

    @Override
    public void stop() {

        // Limpe o inventário de todos os jogadores do evento.
        for(Player p: getPlayers()) {
            p.getInventory().setHelmet(null);
            p.getInventory().clear();
        }

        // Remova o team.
        potato_holder_team.unregister();

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    @Override
    public void leave(Player p) {

        if(getPlayers().contains(p)) {
            for (Player player : getPlayers()) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName()));
            }

            for (Player player : getSpectators()) {
                player.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Leave").replace("&", "§").replace("@player", p.getName()));
            }
        }

        // Se o jogador que saiu for o holder da batata, então pegue outro.
        if(potato_holder == p) {
            potato_holder_team.removePlayer(p);
            potato_holder.getInventory().clear();
            potato_holder.getInventory().setHelmet(null);
            potato_holder = null;
            if((getPlayers().size() - 1) > 1) randomHolder();
        }

        PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);
        this.remove(p);

    }

    private void randomHolder() {
        // Obtenha um usuário aleatório para ser o holder.
        Random random = new Random();
        setHolder(getPlayers().get(random.nextInt(getPlayers().size())), potato_holder_changes);
    }

    public void setHolder(Player p, int current_potato_holder_changes) {

        if(!isHappening()) return;
        if(potato_holder != null) potato_holder_team.removePlayer(potato_holder);

        potato_holder = p;
        Bukkit.getScheduler().cancelTask(task);

        // Dê a batata para o holder e o equipe com TNT.
        potato_holder.getInventory().setHelmet(new ItemStack(Material.TNT, 1));
        for(int i = 0; i < 9; i++) {
            potato_holder.getInventory().setItem(i, XMaterial.POTATO.parseItem());
        }

        // O adicione para o time do batata holder.
        current_potato_holder_changes++;
        potato_holder_changes++;
        potato_holder_team.addPlayer(p);

        // Spawne um foguete na localização do holder.
        Location loc = potato_holder.getLocation();
        Firework firework = potato_holder.getWorld().spawn(loc, Firework.class);
        FireworkMeta data = firework.getFireworkMeta();
        data.addEffects(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL).build());
        data.setPower(2);
        firework.setFireworkMeta(data);

        // Mande a mensagem para todos os jogadores.
        List<String> potato_st = config.getStringList("Messages.Potato");
        for (Player player : getPlayers()) {
            for(String s : potato_st) {
                player.sendMessage(s.replace("&", "§").replace("@player", potato_holder.getName()).replace("@name", this.config.getString("Evento.Title")));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : potato_st) {
                player.sendMessage(s.replace("&", "§").replace("@player", potato_holder.getName()).replace("@name", this.config.getString("Evento.Title")));
            }
        }

        potato_holder.sendMessage(config.getString("Messages.Potato holder").replace("@time", String.valueOf(max_time)).replace("@name", this.config.getString("Evento.Title")).replace("&", "§"));

        int finalCurrent_potato_holder_changes = current_potato_holder_changes;
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(aEventos.getInstance(), new Runnable() {

            int run = 5;
            @Override
            public void run() {
                if(!isHappening()) Bukkit.getScheduler().cancelTask(task);
                if(potato_holder_changes != finalCurrent_potato_holder_changes) Bukkit.getScheduler().cancelTask(task);
                if(run <= 1) Bukkit.getScheduler().cancelTask(task);
                p.sendMessage(config.getString("Messages.Potato explode").replace("&", "§").replace("@time", String.valueOf(run)).replace("@name", config.getString("Evento.Title")));
                run-=1;
            }

        }, (max_time - 5) * 20L, 20L);

        // Se o portador da batata for o mesmo depois do tempo limite, então o elimine e defina outro.
        int current_task_id = task;
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

            if(!isHappening()) return;
            if(!getPlayers().contains(p)) return;
            if(getPotatoHolder() == null || getPotatoHolder() != p) return;
            if(task != current_task_id) return;

            potato_holder.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
            potato_holder.getInventory().setHelmet(null);
            remove(potato_holder);

            PlayerLoseEvent lose = new PlayerLoseEvent(potato_holder, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
            Bukkit.getPluginManager().callEvent(lose);

            potato_holder = null;
            randomHolder();

        }, max_time * 20L);

    }

    public Player getPotatoHolder() {
        return potato_holder;
    }
    public int getPotatoHolderChanges() { return potato_holder_changes; }

}
