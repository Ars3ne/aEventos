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

package com.ars3ne.eventos.inventory.utils;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class SimpleItemParser {

    // Usado apenas em inventários.

    public static ItemStack parse(ConfigurationSection section, Map<String, String> placeholders) {

        ItemStack item = XMaterial.matchXMaterial(section.getString("Material")).get().parseItem();
        assert item != null;
        ItemMeta meta = item.getItemMeta();

        if(section.getString("Name") != null) {

            String name = section.getString("Name");
            if(placeholders != null) {
                for(String placeholder: placeholders.keySet()) {
                    name = name.replace(placeholder, placeholders.get(placeholder));
                }
            }

            meta.setDisplayName(name.replace("&", "§"));

        }

        if(section.getStringList("Lore") != null) {

            List<String> lore = new ArrayList<>();
            for(String s: section.getStringList("Lore")) {

                if(placeholders != null) {
                    for(String placeholder: placeholders.keySet()) {
                        s = s.replace(placeholder, placeholders.get(placeholder));
                    }
                }

                lore.add(s.replace("&", "§"));

            }

            meta.setLore(lore);

        }


        if(section.getBoolean("Glow")) {
            meta.addEnchant(XEnchantment.DURABILITY.parseEnchantment(), 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);

        if(XMaterial.matchXMaterial(section.getString("Material")).get() == XMaterial.PLAYER_HEAD) {

            if(section.getBoolean("Custom head")) {
                return getCustomSkull("http://textures.minecraft.net/texture/" + section.getString("Head data"), item);
            }else {

                SkullMeta skull_meta = (SkullMeta) item.getItemMeta();
                String owner = section.getString("Head data");

                if(placeholders != null) {
                    for(String placeholder: placeholders.keySet()) {
                        owner = owner.replace(placeholder, placeholders.get(placeholder));
                    }
                }

                skull_meta.setOwner(owner);
                item.setItemMeta(skull_meta);
            }
        }

        return item;

    }

    private static ItemStack getCustomSkull(String url, ItemStack item) {

        Field profileField;

        SkullMeta meta = (SkullMeta) item.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] data = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(data)));

        try {
            profileField = meta.getClass().getDeclaredField("profile");

            profileField.setAccessible(true);
            profileField.set(meta, profile);

            item.setItemMeta(meta);

            return item;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
