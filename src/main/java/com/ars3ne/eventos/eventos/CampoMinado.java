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
import com.ars3ne.eventos.listeners.eventos.CampoMinadoListener;
import com.ars3ne.eventos.utils.Cuboid;
import com.ars3ne.eventos.utils.Utils;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class CampoMinado extends Evento {

    private final YamlConfiguration config;
    private final CampoMinadoListener listener = new CampoMinadoListener();

    private final Cuboid cuboid;
    private final Block[] borders;
    private final World world;
    private final BlockFace spawn_direction;

    private BukkitTask runnable;
    private final BukkitScheduler scheduler = aEventos.getInstance().getServer().getScheduler();

    private int level;
    private final int delay;
    private final int max_time;
    private boolean level_happening = false;
    private final boolean last_player_win;

    public CampoMinado(YamlConfiguration config) {

        super(config);
        this.config = config;

        delay = this.config.getInt("Evento.Delay");
        max_time = this.config.getInt("Evento.Time");
        last_player_win = this.config.getBoolean("Evento.Last player win");


        // Obtenha o cuboid
        world = aEventos.getInstance().getServer().getWorld(this.config.getString("Locations.Pos1.world"));
        Location pos1 = new Location(world, this.config.getDouble("Locations.Pos1.x"), this.config.getDouble("Locations.Pos1.y"), this.config.getDouble("Locations.Pos1.z"));
        Location pos2 = new Location(world, this.config.getDouble("Locations.Pos2.x"), this.config.getDouble("Locations.Pos2.y"), this.config.getDouble("Locations.Pos2.z"));
        cuboid = new Cuboid(pos1, pos2);
        borders = cuboid.corners();

        spawn_direction = Utils.yawToFace(this.config.getLong("Locations.Entrance.yaw"), false);

        // Transforme todos os blocos em vidros.
        for (Block b : cuboid.getBlocks()) {
            b.setType(Material.GLASS);
        }

        // Crie uma parede em volta do campo
        addWall();

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Inicie o primeiro nível.

        runnable = scheduler.runTaskTimer(aEventos.getInstance(), () -> {

            if(!isHappening()) runnable.cancel();
            if(!level_happening) {

                if(level < config.getInt("Evento.Levels")) {
                    level();
                }else {
                    win();
                    runnable.cancel();
                }

            }
        }, 0, 20L);

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
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", String.join(", ", winners)).replace("@name", config.getString("Evento.Title"))));
        }

    }

    @Override
    public void stop() {

        // Transforme os blocos em vidro, remova o listener e cancele o evento.
        this.level_happening = false;
        this.removePlayers();
        HandlerList.unregisterAll(listener);

        removeWall();

        for (Block b : cuboid.getBlocks()) {
            b.setType(Material.GLASS);
        }


    }

    private void level() {

        if(isHappening()) {

            level++;
            level_happening = true;

            // Transforme todos os blocos em vidro.
            for (Block b : cuboid.getBlocks()) {
                b.setType(Material.GLASS);
            }

            // Crie uma parede em volta do campo
            addWall();

            // Mande a mensagem de início do nível para todos os participantes.
            List<String> starting_level = config.getStringList("Messages.Starting level");
            for (Player player : getPlayers()) {
                for(String s : starting_level) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@level", String.valueOf(level)).replace("@time", String.valueOf(delay)).replace("@name", this.config.getString("Evento.Title"))));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : starting_level) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@level", String.valueOf(level)).replace("@time", String.valueOf(delay)).replace("@name", this.config.getString("Evento.Title"))));
                }
            }


            // Aguarde o tempo na config para iniciar o nível.
            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

                if(isHappening() && level_happening) {

                    // Mande a mensagem de início do nível para todos os participantes.
                    List<String> starting_level1 = config.getStringList("Messages.Next level");
                    for (Player player : getPlayers()) {
                        for(String s : starting_level1) {
                            player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@level", String.valueOf(level)).replace("@name", config.getString("Evento.Title"))));
                        }
                    }

                    for (Player player : getSpectators()) {
                        for(String s : starting_level1) {
                            player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@level", String.valueOf(level)).replace("@name", config.getString("Evento.Title"))));
                        }
                    }

                    // Coloque os blocos na arena.
                    fill();

                    // Remova as paredes.
                    removeWall();

                }


            }, delay * 20L);

            // Aguarde o tempo na config para terminar o nível atual.
            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

                if(isHappening() && level_happening) {

                    try{

                        // Elimine todos os jogadores dentro do cuboid.
                        List<Player> eliminate = new ArrayList<>();
                        for (Player p : getPlayers()) {

                            if(cuboid.isInWithMargeY(p, 6)) {
                                // Remova o jogador do evento e envie a mensagem de eliminação.
                                eliminate.add(p);
                                continue;
                            }

                            // Inicio da gambiarra. Não estou orgulhoso dela, porém é o que temos para hoje.
                            // Se o nível não for divisível por dois, verifique se o jogador saiu da entrada.

                            if(level % 2 != 0) {

                                switch(spawn_direction) {
                                    case EAST:
                                        // Se a direção do spawn for EAST, verifique se o usuário avançou no X.
                                        if(p.getLocation().getX() < config.getLong("Locations.Entrance.x") + cuboid.getXWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                    case WEST:
                                        // Se a direção do spawn for WEST, verifique se o usuário avançou no X.
                                        if(p.getLocation().getX() > config.getLong("Locations.Entrance.x") - cuboid.getXWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                    case SOUTH:
                                        // Se a direção do spawn for SOUTH, verifique se o usuário avançou no Z.
                                        if (p.getLocation().getZ() < config.getLong("Locations.Entrance.z") + cuboid.getZWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                    case NORTH:
                                        // Se a direção do spawn for NORTH, verifique se o usuário avançou no Z.
                                        if (p.getLocation().getZ() > config.getLong("Locations.Entrance.z") - cuboid.getZWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                }

                            }else {
                                // Se for divisível, verifique se o jogador saiu do outro lado.

                                switch(spawn_direction) {
                                    case EAST:
                                        // Se a direção do spawn for EAST, verifique se o usuário avançou no X.
                                        if(p.getLocation().getX() > config.getLong("Locations.Entrance.x") + cuboid.getXWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                    case WEST:
                                        // Se a direção do spawn for WEST, verifique se o usuário avançou no X.
                                        if(p.getLocation().getX() < config.getLong("Locations.Entrance.x") - cuboid.getXWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                    case SOUTH:
                                        // Se a direção do spawn for SOUTH, verifique se o usuário avançou no Z.
                                        if (p.getLocation().getZ() > config.getLong("Locations.Entrance.z") + cuboid.getZWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                    case NORTH:
                                        // Se a direção do spawn for NORTH, verifique se o usuário avançou no Z.
                                        if (p.getLocation().getZ() < config.getLong("Locations.Entrance.z") - cuboid.getZWidth()) {
                                            eliminate.add(p);
                                        }
                                        break;
                                }
                            }

                        }

                        for(Player p: eliminate) {
                           eliminate(p);
                        }
                        eliminate.clear();

                        level_happening = false;

                    }catch(ConcurrentModificationException e) {
                        e.printStackTrace();
                    }

                }

            }, max_time * 20L);
        }

    }

    private void fill() {

        int percentage = (int) ( cuboid.getTotalBlockSize() * ( this.config.getDouble("Evento.Difficulty") * level / 100.0f ));
        int i = 0;

        // Remova blocos aleatórios.
        while(i < percentage) {
            Block block = cuboid.getRandomLocation().getBlock();
            if(block.getType() == Material.AIR) continue;

            block.setType(Material.AIR);
            i++;
        }

    }

    private void addWall() {

        for(int x = 0; x < cuboid.getXWidth(); x++) {
            for(int z = 0; z < cuboid.getZWidth(); z++) {
                for(int y = 1; y < 6; y++) {
                    world.getBlockAt(new Location(world, borders[0].getX() + x, borders[0].getY() + y, borders[0].getZ())).setType(Material.GLASS);
                    world.getBlockAt(new Location(world, borders[5].getX() - x, borders[5].getY() + y, borders[5].getZ())).setType(Material.GLASS);
                    world.getBlockAt(new Location(world, borders[2].getX(), borders[2].getY() + y, borders[2].getZ() + z)).setType(Material.GLASS);
                    world.getBlockAt(new Location(world, borders[7].getX(), borders[7].getY() + y, borders[7].getZ() - z)).setType(Material.GLASS);
                }
            }
        }

    }

    private void removeWall() {

        for(int x = 0; x < cuboid.getXWidth(); x++) {
            for(int z = 0; z < cuboid.getZWidth(); z++) {
                for(int y = 1; y < 6; y++) {
                    world.getBlockAt(new Location(world, borders[0].getX() + x, borders[0].getY() + y, borders[0].getZ())).setType(Material.AIR);
                    world.getBlockAt(new Location(world, borders[5].getX() - x, borders[5].getY() + y, borders[5].getZ())).setType(Material.AIR);
                    world.getBlockAt(new Location(world, borders[2].getX(), borders[2].getY() + y, borders[2].getZ() + z)).setType(Material.AIR);
                    world.getBlockAt(new Location(world, borders[7].getX(), borders[7].getY() + y, borders[7].getZ() - z)).setType(Material.AIR);
                }
            }
        }

    }

    public void eliminate(Player p) {
        p.sendMessage(IridiumColorAPI.process(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§")));
        remove(p);
        notifyLeave(p);
        PlayerLoseEvent lose = new PlayerLoseEvent(p, getConfig().getString("filename").substring(0, getConfig().getString("filename").length() - 4), getType());
        Bukkit.getPluginManager().callEvent(lose);

        if(isHappening() && getPlayers().size() == 1 && last_player_win) {
            win();
        }
    }

}
