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

package com.ars3ne.eventos.manager;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.utils.InventoryConfigFile;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public class InventorySerializer {

    public static void serialize(Player p, String evento_identifier) {

        if(aEventos.getEventoManager().getEvento() == null) return;
        if(!aEventos.getEventoManager().getEvento().requireEmptyInventory()) return;

        InventoryConfigFile.create(p.getUniqueId().toString());
        InventoryConfigFile.create(p.getUniqueId().toString(), evento_identifier);

        YamlConfiguration config = InventoryConfigFile.get(p.getUniqueId().toString());

        // Salve o inventário do usuário para um arquivo.
        for(int i = 0 ; i < p.getInventory().getSize() ; i++) {
            if(p.getInventory().getItem(i) != null && p.getInventory().getItem(i).getType() != XMaterial.AIR.parseMaterial()) {
                config.set(Integer.toString(i), p.getInventory().getItem(i));
            }
        }

        // Salve o inventário do usuário para um arquivo.
        for(int i = 0 ; i < p.getInventory().getArmorContents().length; i++) {
            if(p.getInventory().getArmorContents()[i] != null && p.getInventory().getArmorContents()[i].getType() != XMaterial.AIR.parseMaterial()) {
                config.set("a" + i, p.getInventory().getArmorContents()[i]);
            }
        }

        try {
            InventoryConfigFile.save(config);
            InventoryConfigFile.save(config, evento_identifier);
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);

        } catch (IOException e) {

            Bukkit.getConsoleSender().sendMessage("§c[aEventos] Ocorreu um erro ao salvar o inventário de um usuário. Cancelando evento...");
            e.printStackTrace();

            YamlConfiguration evento_config;
            evento_config = aEventos.getEventoManager().getEvento().getConfig();
            aEventos.getEventoManager().getEvento().stop();

            List<String> broadcast_messages = evento_config.getStringList("Messages.Cancelled");
            for(String s : broadcast_messages) {
                aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@name", evento_config.getString("Evento.Title")));
            }

        }

    }

    public static void deserialize(Player p, String evento_identifier, boolean leaved) {

        if(aEventos.getEventoManager().getEvento() == null) return;
        if(!aEventos.getEventoManager().getEvento().requireEmptyInventory()) return;
        if(!InventoryConfigFile.exists(p.getUniqueId().toString())) return;

        YamlConfiguration config = InventoryConfigFile.get(p.getUniqueId().toString());
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        for(String keys : config.getKeys(false)) {

            if(keys.equalsIgnoreCase("filename") || keys.equalsIgnoreCase("filenme")) continue;

            if(keys.contains("a")) {

                String key = keys.replace("a", "");
                int slot = Integer.parseInt(key);
                ItemStack item = config.getItemStack(keys);

                ItemStack[] armor = p.getInventory().getArmorContents();
                armor[slot] = item;

                p.getInventory().setArmorContents(armor);

            }else {
                int slot = Integer.parseInt(keys);
                ItemStack item = config.getItemStack(keys);
                p.getInventory().setItem(slot, item);
            }
        }

        // Delete o arquivo do inventário.
        InventoryConfigFile.delete(config);
        if(leaved) InventoryConfigFile.delete(config, evento_identifier);
    }

}
