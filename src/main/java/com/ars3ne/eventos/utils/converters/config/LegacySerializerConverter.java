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
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class LegacySerializerConverter {

    // Converte para o antigo serializer. Caso queira converter para o novo, utilize a classe SerializerConverter.

    public static boolean convertFight(YamlConfiguration config) {

        // Se for a config antiga, converta os items.
        if(config.getString("Items.Normal.Armor.Helmet.Material") == null) {

            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConvertendo o arquivo de configuração §f" + config.getString("filename") + " §apara a nova versão...");

            // Luta normal
            List<String> normal_items = new ArrayList<>();
            for (String s : config.getStringList("Items.Normal.Inventory")) {
                String[] separated = s.split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                separated[0] = replace.toString();
                normal_items.add(String.join("-", separated));
            }
            config.set("Items.Normal.Inventory", normal_items);

            if (config.getString("Items.Normal.Armor.Helmet") != null) {
                String[] separated = config.getString("Items.Normal.Armor.Helmet").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Normal.Armor.Helmet.Material", replace.toString());
            }else {
                config.set("Items.Normal.Armor.Helmet.Material", "null");
            }
            config.set("Items.Normal.Armor.Helmet.Data", 0);

            if (config.getString("Items.Normal.Armor.Chestplate") != null) {
                String[] separated = config.getString("Items.Normal.Armor.Chestplate").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Normal.Armor.Chestplate.Material", replace.toString());
            }else {
                config.set("Items.Normal.Armor.Chestplate.Material", "null");
            }
            config.set("Items.Normal.Armor.Chestplate.Data", 0);

            if (config.getString("Items.Normal.Armor.Legging") != null) {
                String[] separated = config.getString("Items.Normal.Armor.Legging").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Normal.Armor.Legging.Material", replace.toString());
            }else {
                config.set("Items.Normal.Armor.Legging.Material", "null");
            }
            config.set("Items.Normal.Armor.Legging.Data", 0);

            if (config.getString("Items.Normal.Armor.Boots") != null) {
                String[] separated = config.getString("Items.Normal.Armor.Boots").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Normal.Armor.Boots.Material", replace.toString());
            }else {
                config.set("Items.Normal.Armor.Boots.Material", "null");
            }
            config.set("Items.Normal.Armor.Boots.Data", 0);

            // Última luta
            List<String> last_items = new ArrayList<>();
            for (String s : config.getStringList("Items.Last fight.Inventory")) {
                String[] separated = s.split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                separated[0] = replace.toString();
                last_items.add(String.join("-", separated));
            }
            config.set("Items.Last fight.Inventory", last_items);

            if (config.getString("Items.Last fight.Armor.Helmet") != null) {
                String[] separated = config.getString("Items.Last fight.Armor.Helmet").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Last fight.Armor.Helmet.Material", replace.toString());
            }else {
                config.set("Items.Last fight.Armor.Helmet.Material", "null");
            }
            config.set("Items.Last fight.Armor.Helmet.Data", 0);

            if (config.getString("Items.Last fight.Armor.Chestplate") != null) {
                String[] separated = config.getString("Items.Last fight.Armor.Chestplate").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Last fight.Armor.Chestplate.Material", replace.toString());
            }else {
                config.set("Items.Last fight.Armor.Chestplate.Material", "null");
            }
            config.set("Items.Last fight.Armor.Chestplate.Data", 0);

            if (config.getString("Items.Last fight.Armor.Legging") != null) {
                String[] separated = config.getString("Items.Last fight.Armor.Legging").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Last fight.Armor.Legging.Material", replace.toString());
            }else {
                config.set("Items.Last fight.Armor.Legging.Material", "null");
            }
            config.set("Items.Last fight.Armor.Legging.Data", 0);

            if (config.getString("Items.Last fight.Armor.Boots") != null) {
                String[] separated = config.getString("Items.Last fight.Armor.Boots").split("-");
                Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                assert replace != null;
                config.set("Items.Last fight.Armor.Boots.Material", replace.toString());
            }else {
                config.set("Items.Last fight.Armor.Boots.Material", "null");
            }
            config.set("Items.Last fight.Armor.Boots.Data", 0);

            try {
                EventoConfigFile.save(config);
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aArquivo §f" + config.getString("filename") + " §aconvertido com sucesso!");
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível converter o arquivo de configuração.");
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    public static boolean convertSpleef(YamlConfiguration config) {

        List<String> items = config.getStringList("Items");
        if(items != null) {

            // Se for a config antiga, converta os items.
            try{

                Integer.parseInt(items.get(0).split("-")[0]);

                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConvertendo o arquivo de configuração §f" + config.getString("filename") + " §apara a nova versão...");

                List<String> new_items = new ArrayList<>();
                for(String item: items) {
                    String[] separated = item.split("-");
                    Material replace = XMaterial.matchXMaterial(Integer.parseInt(separated[0]), (byte) 0).get().parseMaterial();
                    assert replace != null;
                    separated[0] = replace.toString();
                    new_items.add(String.join("-", separated));
                }

                config.set("Items", new_items);

                try {
                    EventoConfigFile.save(config);
                    Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aArquivo §f" + config.getString("filename") + " §aconvertido com sucesso!");
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível converter o arquivo de configuração.");
                    e.printStackTrace();
                }

                return true;

            }catch(NumberFormatException ignored) { }

        }

        return false;

    }

}
