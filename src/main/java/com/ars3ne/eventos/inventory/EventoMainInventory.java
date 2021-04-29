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

package com.ars3ne.eventos.inventory;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.inventory.utils.SimpleItemParser;
import com.ars3ne.eventos.manager.InventoryManager;
import com.ars3ne.eventos.utils.EventoConfigFile;
import com.ars3ne.eventos.utils.MenuConfigFile;
import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.simple.SimpleInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import com.henryfabio.minecraft.inventoryapi.viewer.configuration.ViewerConfiguration;
import com.henryfabio.minecraft.inventoryapi.viewer.impl.simple.SimpleViewer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventoMainInventory extends SimpleInventory {

    private YamlConfiguration config = MenuConfigFile.get("main");

    public EventoMainInventory() {
        super(
                "aeventos.inventory.main",
                "&8Eventos",
                27
        );

    }

    @Override
    protected void configureViewer(SimpleViewer viewer) {
        ViewerConfiguration configuration = viewer.getConfiguration();
        configuration.titleInventory(config.getString("Menu.Name").replace("@jogador", viewer.getName()).replace("&", "§"));
        configuration.inventorySize(config.getInt("Menu.Size"));
    }


    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {

        int total_wins = aEventos.getCache().getPlayerWins(viewer.getPlayer()).values().stream().reduce(0, Integer::sum);
        int total_participations = aEventos.getCache().getPlayerParticipations(viewer.getPlayer()).values().stream().reduce(0, Integer::sum);

        int player_top_wins_position = 0;
        int player_top_participations_position = 0;

        if(total_wins > 0) player_top_wins_position = aEventos.getCache().getPlayerTopWinsPosition(viewer.getPlayer());
        if(total_participations > 0) player_top_participations_position = aEventos.getCache().getPlayerTopParticipationsPosition(viewer.getPlayer());

        // Configure as Placeholders.
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("@player", viewer.getName());
        placeholders.put("@wins", String.valueOf(total_wins));
        placeholders.put("@participations", String.valueOf(total_participations));
        placeholders.put("@wins_position", String.valueOf(player_top_wins_position));
        placeholders.put("@participations_position", String.valueOf(player_top_participations_position));

        if(config.getBoolean("Menu.Items.Profile.Enabled")) {

            ItemStack item = SimpleItemParser.parse(config.getConfigurationSection("Menu.Items.Profile"), placeholders);
            ItemMeta meta = item.getItemMeta();

            if(config.getBoolean("Eventos.Enabled")) {

                List<String> lore = meta.getLore();

                for(String s: config.getStringList("Eventos.List")) {

                    String[] separated = s.split(":");
                    if(EventoConfigFile.exists(separated[0])) {

                        Map<String, Integer> player_wins = aEventos.getCache().getPlayerWins(viewer.getPlayer());
                        Map<String, Integer> player_participations = aEventos.getCache().getPlayerParticipations(viewer.getPlayer());

                        int wins = 0;
                        if(player_wins.containsKey(separated[0])) wins = player_wins.get(separated[0]);

                        int participations = 0;
                        if(player_participations.containsKey(separated[0])) participations = player_participations.get(separated[0]);

                        if(config.getBoolean("Eventos.Only with wins") && wins == 0 && participations == 0) continue;
                        lore.add(config.getString("Eventos.Format").replace("@evento_name", separated[1]).replace("@evento_wins", String.valueOf(wins)).replace("@evento_participations", String.valueOf(participations)).replace("&", "§"));

                    }

                }
                if(config.getBoolean("Eventos.New line")) lore.add("");
                meta.setLore(lore);

            }

            item.setItemMeta(meta);
            editor.setItem(config.getInt("Menu.Items.Profile.Slot"), InventoryItem.of(item));
        }

        if(config.getBoolean("Menu.Items.Eventos.Enabled")) {
            InventoryItem item = InventoryItem.of(SimpleItemParser.parse(config.getConfigurationSection("Menu.Items.Eventos"), placeholders));
            item.defaultCallback(event -> InventoryManager.openEventoListInventory(viewer.getPlayer()) );
            editor.setItem(config.getInt("Menu.Items.Eventos.Slot"), item);
        }

        // Se o item do jogador estiver ativo, então o adicione.
        if(config.getBoolean("Menu.Items.Top.Enabled")) {
            InventoryItem item = InventoryItem.of(SimpleItemParser.parse(config.getConfigurationSection("Menu.Items.Top"), placeholders));
            item.defaultCallback(event -> InventoryManager.openEventoTopInventory(viewer.getPlayer()) );
            editor.setItem(config.getInt("Menu.Items.Top.Slot"), item);
        }

    }

    public void updateConfig() {
        config = MenuConfigFile.get("main");
    }

}