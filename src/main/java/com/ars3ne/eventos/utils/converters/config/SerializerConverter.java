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

package com.ars3ne.eventos.utils.converters.config;

import com.ars3ne.eventos.utils.EventoConfigFile;
import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class SerializerConverter {

    public static void convert(YamlConfiguration config) {

        if(config.getConfigurationSection("Itens") != null) return;

        if(config.getConfigurationSection("Itens.Helmet.damage") == null) {

            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConvertendo o arquivo de configuração §f" + config.getString("filename") + " §apara a nova versão...");

            // Se o evento já tem armadura, então á converta.
            if(config.getConfigurationSection("Items.Normal.Armor") != null && (config.getString("Items.Normal.Armor.Helmet.damage") == null && config.getString("Items.Last fight.Armor.Helmet.damage") == null)) {

                // Converta a armadura normal.
                ConfigurationSection normal = config.getConfigurationSection("Items.Normal.Armor");
                config.set("Items.Normal.Armor", null);

                config.set("Itens.Normal.Armor.Helmet.a", "shaark");
                config.set("Itens.Normal.Armor.Chestplate.a", "shaark");
                config.set("Itens.Normal.Armor.Leggings.a", "shaark");
                config.set("Itens.Normal.Armor.Boots.a", "shaark");

                if (!normal.getString("Helmet.Material").equals("null")) {
                    ItemStack helmet = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(normal.getString("Helmet.Material")).get().parseMaterial()), 1, (byte) normal.getInt("Helmet.Data"));
                    XItemStack.serialize(helmet, config.getConfigurationSection("Itens.Normal.Armor.Helmet"));
                }

                if (!normal.getString("Chestplate.Material").equals("null")) {
                    ItemStack chestplate = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(normal.getString("Chestplate.Material")).get().parseMaterial()), 1, (byte) normal.getInt("Chestplate.Data"));
                    XItemStack.serialize(chestplate, config.getConfigurationSection("Itens.Normal.Armor.Chestplate"));
                }

                if (!normal.getString("Legging.Material").equals("null")) {
                    ItemStack leggings = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(normal.getString("Legging.Material")).get().parseMaterial()), 1, (byte) normal.getInt("Legging.Data"));
                    XItemStack.serialize(leggings, config.getConfigurationSection("Itens.Normal.Armor.Leggings"));
                }

                if (!normal.getString("Boots.Material").equals("null")) {
                    ItemStack boots = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(normal.getString("Boots.Material")).get().parseMaterial()), 1, (byte) normal.getInt("Boots.Data"));
                    XItemStack.serialize(boots, config.getConfigurationSection("Itens.Normal.Armor.Boots"));
                }

                config.set("Itens.Normal.Armor.Helmet.a", null);
                config.set("Itens.Normal.Armor.Chestplate.a", null);
                config.set("Itens.Normal.Armor.Leggings.a", null);
                config.set("Itens.Normal.Armor.Boots.a", null);

                // Converta a armadura final.
                ConfigurationSection last = config.getConfigurationSection("Items.Last fight.Armor");
                config.set("Items.Last fight.Armor", null);

                config.set("Itens.Last fight.Armor.Helmet.a", "shaark");
                config.set("Itens.Last fight.Armor.Chestplate.a", "shaark");
                config.set("Itens.Last fight.Armor.Leggings.a", "shaark");
                config.set("Itens.Last fight.Armor.Boots.a", "shaark");

                if (!last.getString("Helmet.Material").equals("null")) {
                    ItemStack helmet_last = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(last.getString("Helmet.Material")).get().parseMaterial()), 1, (byte) last.getInt("Helmet.Data"));
                    XItemStack.serialize(helmet_last, config.getConfigurationSection("Itens.Last fight.Armor.Helmet"));
                }

                if (!last.getString("Helmet.Material").equals("null")) {
                    ItemStack helmet_last = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(last.getString("Helmet.Material")).get().parseMaterial()), 1, (byte) last.getInt("Helmet.Data"));
                    XItemStack.serialize(helmet_last, config.getConfigurationSection("Itens.Last fight.Armor.Helmet"));
                }

                if (!last.getString("Chestplate.Material").equals("null")) {
                    ItemStack chestplate_last = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(last.getString("Chestplate.Material")).get().parseMaterial()), 1, (byte) last.getInt("Chestplate.Data"));
                    XItemStack.serialize(chestplate_last, config.getConfigurationSection("Itens.Last fight.Armor.Chestplate"));
                }

                if (!last.getString("Legging.Material").equals("null")) {
                    ItemStack leggings_last = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(last.getString("Legging.Material")).get().parseMaterial()), 1, (byte) last.getInt("Legging.Data"));
                    XItemStack.serialize(leggings_last, config.getConfigurationSection("Itens.Last fight.Armor.Leggings"));
                }

                if (!last.getString("Boots.Material").equals("null")) {
                    ItemStack boots_last = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(last.getString("Boots.Material")).get().parseMaterial()), 1, (byte) last.getInt("Boots.Data"));
                    XItemStack.serialize(boots_last, config.getConfigurationSection("Itens.Last fight.Armor.Boots"));
                }

                config.set("Itens.Last fight.Armor.Helmet.a", null);
                config.set("Itens.Last fight.Armor.Chestplate.a", null);
                config.set("Itens.Last fight.Armor.Leggings.a", null);
                config.set("Itens.Last fight.Armor.Boots.a", null);

                // Converta os itens do inventário.
                List<String> items_normal = config.getStringList("Items.Normal.Inventory");
                config.set("Items.Normal.Inventory", null);
                config.set("Itens.Normal.Inventory", "");

                for(int i = 0; i < items_normal.size(); i++) {

                    String s = items_normal.get(i);

                    String[] separated = s.split("-");
                    ItemStack is;

                    if(separated.length == 3) {
                        is = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(separated[0]).get().parseMaterial()), Integer.parseInt(separated[2]), (byte) Integer.parseInt(separated[1]));
                    }else {
                        is = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(separated[0]).get().parseMaterial()), Integer.parseInt(separated[1]));
                    }

                    config.set("Itens.Normal.Inventory." + i + ".a", "shaark");
                    XItemStack.serialize(is, config.getConfigurationSection("Itens.Normal.Inventory." + i));
                    config.set("Itens.Normal.Inventory." + i + ".a", null);

                }

                List<String> last_fight = config.getStringList("Items.Last fight.Inventory");
                config.set("Items.Last fight.Inventory", null);
                config.set("Itens.Last fight.Inventory", "");

                for(int i = 0; i < last_fight.size(); i++) {

                    String s = last_fight.get(i);

                    String[] separated = s.split("-");
                    ItemStack is;

                    if(separated.length == 3) {
                        is = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(separated[0]).get().parseMaterial()), Integer.parseInt(separated[2]), (byte) Integer.parseInt(separated[1]));
                    }else {
                        is = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(separated[0]).get().parseMaterial()), Integer.parseInt(separated[1]));
                    }

                    config.set("Itens.Last fight.Inventory." + i + ".a", "shaark");
                    XItemStack.serialize(is, config.getConfigurationSection("Itens.Last fight.Inventory." + i));
                    config.set("Itens.Last fight.Inventory." + i + ".a", null);

                }

            }else {

                config.set("Itens.Armor.Helmet", "");
                config.set("Itens.Armor.Chestplate", "");
                config.set("Itens.Armor.Leggings", "");
                config.set("Itens.Armor.Boots", "");

                // Converta os itens do inventário.
                List<String> items = config.getStringList("Items");
                config.set("Items.Inventory", null);

                for(int i = 0; i < items.size(); i++) {

                    String s = items.get(i);

                    String[] separated = s.split("-");
                    ItemStack is;

                    if(separated.length == 3) {
                        is = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(separated[0]).get().parseMaterial()), Integer.parseInt(separated[2]), (byte) Integer.parseInt(separated[1]));
                    }else {
                        is = new ItemStack(Objects.requireNonNull(XMaterial.matchXMaterial(separated[0]).get().parseMaterial()), Integer.parseInt(separated[1]));
                    }

                    config.set("Itens.Inventory." + i + ".a", "shaark");
                    XItemStack.serialize(is, config.getConfigurationSection("Itens.Inventory." + i));
                    config.set("Itens.Inventory." + i + ".a", null);

                }

            }

            config.set("Items", null);

            try {
                EventoConfigFile.save(config);
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aArquivo §f" + config.getString("filename") + " §aconvertido com sucesso!");
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível converter o arquivo de configuração.");
                e.printStackTrace();
            }

        }

    }
}
