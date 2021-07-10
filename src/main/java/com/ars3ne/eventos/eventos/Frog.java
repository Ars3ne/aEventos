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
import com.ars3ne.eventos.listeners.eventos.FrogListener;
import com.ars3ne.eventos.utils.Cuboid;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.*;

@SuppressWarnings("deprecation")
public class Frog extends Evento {
    
    private final YamlConfiguration config;
    private final FrogListener listener = new FrogListener();

    private final Map<Block, Material> current_blocks = new HashMap<>();
    private final Map<Block, Map<Material, Byte>> deleted_blocks = new HashMap<>();
    private final Multimap<Material, Byte> remeaning_materials = ArrayListMultimap.create();

    private final Cuboid cuboid;
    private Block wool_block;

    private final int start;
    private final int time;
    private final int snow_time;
    private int task;
    private boolean level_happening;
    final Random random = new Random();

    public Frog(YamlConfiguration config) {
        super(config);

        this.config = config;
        this.start = config.getInt("Evento.Start");
        this.time = config.getInt("Evento.Time");
        this.snow_time = config.getInt("Evento.Snow");
        this.level_happening = false;

        // Obtenha o cuboid.
        World world = aEventos.getInstance().getServer().getWorld(config.getString("Locations.Pos1.world"));
        Location pos1 = new Location(world, config.getDouble("Locations.Pos1.x"), config.getDouble("Locations.Pos1.y"), config.getDouble("Locations.Pos1.z"));
        Location pos2 = new Location(world, config.getDouble("Locations.Pos2.x"), config.getDouble("Locations.Pos2.y"), config.getDouble("Locations.Pos2.z"));
        this.cuboid = new Cuboid(pos1, pos2);

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Obtenha todos os blocos do cuboid e adicione-os a lista. Se não existir, coloque neve.
        for(Block block: cuboid.getBlocks()) {

            if(block.getType() != Material.AIR && block.getType() != Material.SNOW_BLOCK) {

                if(!XMaterial.isNewVersion() && block.getType() == XMaterial.RED_WOOL.parseMaterial() && block.getData() == XMaterial.RED_WOOL.getData()) continue;
                else if(XMaterial.isNewVersion() && block.getType() == XMaterial.RED_WOOL.parseMaterial()) continue;

                current_blocks.put(block, block.getType());

                if(remeaning_materials.containsKey(block.getType()) && !XMaterial.isNewVersion()) {

                    boolean exists = false;

                    for(byte mat: remeaning_materials.get(block.getType())) {
                        if(mat == block.getData()) exists = true;
                    }

                   if(!exists) remeaning_materials.put(block.getType(), block.getData());
                   continue;

                }else if(!XMaterial.isNewVersion()){
                    remeaning_materials.put(block.getType(), block.getData());
                }

                if(XMaterial.isNewVersion() && !remeaning_materials.containsKey(block.getType())) remeaning_materials.put(block.getType(), (byte) 0);

            }else {
                block.setType(Material.SNOW_BLOCK);
            }

        }

        // Depois do período inicial, remova os blocos de neve e inicie o evento.
        aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {
            for(Block block: cuboid.getBlocks()) {
                if(block.getType() == Material.SNOW_BLOCK) block.setType(Material.AIR);
            }

            task = Bukkit.getScheduler().scheduleSyncRepeatingTask(aEventos.getInstance(), () -> {
                if(!isHappening()) Bukkit.getScheduler().cancelTask(task);
                if(level_happening) return;
                frog();
            }, (time + snow_time) * 20L, 20L);

        }, start * 20L);

    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", config.getString("Evento.Title"))));
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

        // Remova todos os blocos de neve.
        for(Block block: cuboid.getBlocks()) {
            if(block.getType() == Material.SNOW_BLOCK) block.setType(Material.AIR);
        }

        // Coloque todos os blocos novamente.
        for(Block block: deleted_blocks.keySet()) {
            Map<Material, Byte> hash = deleted_blocks.get(block);
            block.setType((Material) hash.keySet().toArray()[0]);
            if(!XMaterial.isNewVersion()) block.setData((Byte) hash.values().toArray()[0]);
        }

        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    private void frog() {

        if(!isHappening()) return;
        level_happening = true;

        if(remeaning_materials.size() > 1) {

            // Obtenha um material aleatório e transforme todos o do seu tipo em neve.
            int index = random.nextInt(remeaning_materials.size());
            Material material_remove = (Material) remeaning_materials.keys().toArray()[index];
            byte material_data = (byte) remeaning_materials.values().toArray()[index];

            // Transforme todos o do seu tipo em neve.
            for(Block b: current_blocks.keySet()) {
                if(b.getType() == material_remove && (material_data == (byte) 0 || material_data == b.getData())) {

                    HashMap<Material, Byte> hash = new HashMap<>();
                    hash.put(material_remove, material_data);

                    deleted_blocks.put(b, hash);

                    b.setType(Material.SNOW_BLOCK);
                }
            }

            // Depois do tempo especificado na config, remova os blocos.
            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

                if(!isHappening()) return;
                remeaning_materials.remove(material_remove, material_data);

                for(Block b: deleted_blocks.keySet()) {
                    b.setType(Material.AIR);
                    current_blocks.remove(b);
                }

            }, snow_time * 20L);

            // Finalize o ciclo.
            aEventos.getInstance().getServer().getScheduler().runTaskLater(aEventos.getInstance(), () -> this.level_happening = false, (time + snow_time) * 20L);

        }else {

            // Se tiver apenas um tipo de bloco, coloque a lã vermelha em um bloco removido aleatório.
            List<Block> deleted_blocks_array = new ArrayList<>(deleted_blocks.keySet());
            wool_block = deleted_blocks_array.get(random.nextInt(deleted_blocks_array.size()));
            wool_block.setType(XMaterial.RED_WOOL.parseMaterial());
            if(!XMaterial.isNewVersion()) wool_block.setData(XMaterial.RED_WOOL.getData());
            listener.setWool();

            // Troque os outros blocos deletados por neve.
            for(Block b: deleted_blocks.keySet()) {
                if(b == wool_block) continue;
                b.setType(Material.SNOW_BLOCK);
            }

            // Mande a mensagem para todos os participantes.
            List<String> wool_st = config.getStringList("Messages.Wool");
            for (Player player : getPlayers()) {
                for(String s : wool_st) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : wool_st) {
                    player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@name", config.getString("Evento.Title"))));
                }
            }

        }

    }

    public Block getWoolBlock() {
        return wool_block;
    }

}
