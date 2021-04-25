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
import com.ars3ne.eventos.listeners.eventos.AnvilListener;
import com.ars3ne.eventos.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Anvil extends Evento {

    private final YamlConfiguration config;
    private final AnvilListener listener = new AnvilListener();

    private final List<Block> anvil = new ArrayList<>();

    private final int height, time, delay;

    private final Cuboid cuboid;

    private BukkitTask runnable;
    private final BukkitScheduler scheduler = aEventos.getInstance().getServer().getScheduler();

    public Anvil(YamlConfiguration config) {
        super(config);
        this.config = config;

        this.height = config.getInt("Evento.Height");
        this.time = config.getInt("Evento.Time");
        this.delay = config.getInt("Evento.Delay");

        // Obtenha o cuboid.
        World world = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Pos1.world"));
        Location pos1 = new Location(world, config.getDouble("Locations.Pos1.x"), config.getDouble("Locations.Pos1.y"), config.getDouble("Locations.Pos1.z"));
        Location pos2 = new Location(world, config.getDouble("Locations.Pos2.x"), config.getDouble("Locations.Pos2.y"), config.getDouble("Locations.Pos2.z"));
        this.cuboid = new Cuboid(pos1, pos2);

        // Remova as bigornas.
        for(Block block: cuboid.getBlocks()) {
            if(block.getRelative(0, 1, 0).getType() == Material.ANVIL) {
                block.getRelative(0, 1, 0).setType(Material.AIR);
            }
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void start() {
        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Remova as bigornas.
        for(Block block: cuboid.getBlocks()) {
            if(block.getType() == Material.ANVIL) {
                block.setType(Material.AIR);
            }
        }

        // Envie a mensagem para todos os usuários no evento.
        List<String> starting_level = config.getStringList("Messages.Starting");
        for (Player player : getPlayers()) {
            for(String s : starting_level) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(time)).replace("@name", config.getString("Evento.Title")));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : starting_level) {
                player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(time)).replace("@name", config.getString("Evento.Title")));
            }
        }

        // Adicione o efeito jump boost para não permitir que os jogadores pulem.
        for(Player p: getPlayers()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200));
        }

        // Depois do tempo da config, inicie o evento.
        runnable = scheduler.runTaskTimer(aEventos.getInstance(), () -> {

            if(!isHappening()) runnable.cancel();

            if(anvil.size() < cuboid.getTotalBlockSize() - 1) {

                // Obtenha um bloco aleatório do cuboid.
                Block anvil_block = cuboid.getRandomLocation().getBlock();
                if (anvil_block.getType() != Material.AIR || anvil.contains(anvil_block)) {
                    for(int i = 0; i < cuboid.getTotalBlockSize() - 1; i++) {
                        anvil_block = cuboid.getRandomLocation().getBlock();
                        if(anvil_block.getType() == Material.AIR) break;
                    }
                }

                anvil.add(anvil_block.getRelative(0, 1 ,0));
                FallingBlock falling_anvil = anvil_block.getWorld().spawnFallingBlock(anvil_block.getLocation().add(0, height, 0), Material.ANVIL, (byte) 0);
                falling_anvil.setDropItem(false);

            }else {
                win();
                runnable.cancel();
            }

        }, time * 20L, delay * 20L);

    }

    public void win() {

        List<String> winners = new ArrayList<>();

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinners();

        // Encerre o evento.
        this.stop();

        // Obtenha todos os jogadores restantes e os entregue as recompensas.
        for(Player p: getPlayers()) {

            // Execute os comandos de vitória
            List<String> commands = this.config.getStringList("Rewards.Commands");
            for(String s : commands) {
                executeConsoleCommand(p, s.replace("@winner", p.getName()));
            }

            // Adicione o nome á lista de vencedores.
            winners.add(p.getName());
        }

        // Mande a mensagem de vitória para o servidor.
        List<String> broadcast_messages = this.config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", String.join(", ", winners)).replace("@name", config.getString("Evento.Title")));
        }

    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", getConfig().getString("Evento.Title")));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = config.getStringList("Rewards.Commands");
        for(String s : commands) {
            executeConsoleCommand(p, s.replace("@winner", p.getName()));
        }

    }

    @Override
    public void stop() {

        // Remova o efeito.
        for(Player p: getPlayers()) {
            p.removePotionEffect(PotionEffectType.JUMP);
        }

        // Remova as bigornas.
        for(Block block: cuboid.getBlocks()) {
            if(block.getRelative(0, 1, 0).getType() == Material.ANVIL) {
                block.getRelative(0, 1, 0).setType(Material.AIR);
            }
        }

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    public List<Block> getAnvils() {
        return this.anvil;
    }

}
