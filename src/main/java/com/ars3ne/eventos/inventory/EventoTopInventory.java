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
import com.ars3ne.eventos.utils.EventoConfigFile;
import com.ars3ne.eventos.utils.MenuConfigFile;
import com.cryptomorin.xseries.XMaterial;
import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.paged.PagedInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.item.supplier.InventoryItemSupplier;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import com.henryfabio.minecraft.inventoryapi.viewer.configuration.impl.ViewerConfigurationImpl;
import com.henryfabio.minecraft.inventoryapi.viewer.impl.paged.PagedViewer;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class EventoTopInventory extends PagedInventory {

    private YamlConfiguration config = MenuConfigFile.get("top_players");
    private final Map<String, Integer> player_filter = new HashMap<>();

    public EventoTopInventory() {
        super(
                "aeventos.inventory.eventotop",
                "&8Eventos",
                27
        );

    }

    @Override
    protected void configureViewer(PagedViewer viewer) {
        ViewerConfigurationImpl.Paged configuration = viewer.getConfiguration();
        configuration.inventorySize(config.getInt("Menu.Size"));
        configuration.nextPageSlot(config.getInt("Menu.Next page slot"));
        configuration.previousPageSlot(config.getInt("Menu.Previous page slot"));
        configuration.emptyPageSlot(config.getInt("Menu.Empty page slot"));
        configuration.itemPageLimit(config.getInt("Menu.Item page limit"));

        AtomicInteger current_filter = new AtomicInteger(player_filter.getOrDefault(viewer.getName(), -1));
        if(current_filter.get() == -1) configuration.titleInventory(config.getString("Filter.Names.Wins").replace("@jogador", viewer.getName()).replace("&", "§"));
        if(current_filter.get() == 0) configuration.titleInventory(config.getString("Filter.Names.Participations").replace("@jogador", viewer.getName()).replace("&", "§"));

    }

    @Override
    protected List<InventoryItemSupplier> createPageItems(PagedViewer viewer) {

        List<InventoryItemSupplier> itemSuppliers = new LinkedList<>();
        AtomicInteger current_filter = new AtomicInteger(player_filter.getOrDefault(viewer.getName(), -1));

        int position = 1;

        // Vitórias.
        if(current_filter.get() == -1) {

            List<OfflinePlayer> players = new ArrayList<>();

            for(OfflinePlayer p: aEventos.getCacheManager().getPlayerTopWinsList().keySet()) {

                if(players.contains(p)) continue;
                players.add(p);

                List<String> lore = new ArrayList<>();

                ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
                assert item != null;
                SkullMeta meta = (SkullMeta) item.getItemMeta();

                meta.setOwner(p.getName());
                meta.setDisplayName(config.getString("Menu.Items.Player.Name").replace("@top_player", p.getName()).replace("&", "§"));

                int total_wins = aEventos.getCacheManager().getPlayerWins(p) != null ? aEventos.getCacheManager().getPlayerWins(p).values().stream().reduce(0, Integer::sum) : 0;
                int total_participations = aEventos.getCacheManager().getPlayerParticipations(p) != null ? aEventos.getCacheManager().getPlayerParticipations(p).values().stream().reduce(0, Integer::sum) : 0;

                int player_top_wins_position = aEventos.getCacheManager().getPlayerTopWinsPosition(p);
                int player_top_participations_position = aEventos.getCacheManager().getPlayerTopParticipationsPosition(p);

                for(String s: config.getStringList("Menu.Items.Player.Lore")) {
                    lore.add(s.replace("@position", String.valueOf(position)).replace("@total_wins", String.valueOf(total_wins)).replace("@total_participations", String.valueOf(total_participations)).replace("@wins_position", String.valueOf(player_top_wins_position)).replace("@participations_position", String.valueOf(player_top_participations_position)).replace("&", "§"));
                }

                if(config.getBoolean("Eventos.Enabled")) {

                    boolean has_win_or_victory = false;

                    for(String s: config.getStringList("Eventos.List")) {

                        String[] separated = s.split(":");
                        if(EventoConfigFile.exists(separated[0])) {

                            Map<String, Integer> player_wins = aEventos.getCacheManager().getPlayerWins(p);
                            Map<String, Integer> player_participations = aEventos.getCacheManager().getPlayerParticipations(p);

                            int wins = 0;
                            if(player_wins != null && player_wins.containsKey(separated[0])) wins = player_wins.get(separated[0]);

                            int participations = 0;
                            if(player_participations != null && player_participations.containsKey(separated[0])) participations = player_participations.get(separated[0]);

                            if(config.getBoolean("Eventos.Only with wins") && wins == 0 && participations == 0) continue;
                            has_win_or_victory = true;
                            lore.add(config.getString("Eventos.Format").replace("@evento_name", separated[1]).replace("@evento_wins", String.valueOf(wins)).replace("@evento_participations", String.valueOf(participations)).replace("&", "§"));

                        }
                    }

                    if(!has_win_or_victory) {
                        lore.add(config.getString("Eventos.Empty").replace("&", "§"));
                    }

                    if(config.getBoolean("Eventos.New line")) lore.add("");

                }

                meta.setLore(lore);
                item.setItemMeta(meta);
                itemSuppliers.add(() -> InventoryItem.of(item));

                position++;

            }

        }

        // Participações.
        if(current_filter.get() == 0) {

            List<OfflinePlayer> players = new ArrayList<>();

            for(OfflinePlayer p: aEventos.getCacheManager().getPlayerTopParticipationsList().keySet()) {

                if(players.contains(p)) continue;
                players.add(p);

                List<String> lore = new ArrayList<>();

                ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
                assert item != null;
                SkullMeta meta = (SkullMeta) item.getItemMeta();

                meta.setOwner(p.getName());
                meta.setDisplayName(config.getString("Menu.Items.Player.Name").replace("@top_player", p.getName()).replace("&", "§"));

                int total_wins = aEventos.getCacheManager().getPlayerWins(p) != null ? aEventos.getCacheManager().getPlayerWins(p).values().stream().reduce(0, Integer::sum) : 0;
                int total_participations = aEventos.getCacheManager().getPlayerParticipations(p) != null ? aEventos.getCacheManager().getPlayerParticipations(p).values().stream().reduce(0, Integer::sum) : 0;

                int player_top_wins_position = aEventos.getCacheManager().getPlayerTopWinsPosition(p);
                int player_top_participations_position = aEventos.getCacheManager().getPlayerTopParticipationsPosition(p);

                for(String s: config.getStringList("Menu.Items.Player.Lore")) {
                    lore.add(s.replace("@position", String.valueOf(position)).replace("@total_wins", String.valueOf(total_wins)).replace("@total_participations", String.valueOf(total_participations)).replace("@wins_position", String.valueOf(player_top_wins_position)).replace("@participations_position", String.valueOf(player_top_participations_position)).replace("&", "§"));
                }

                if(config.getBoolean("Eventos.Enabled")) {

                    boolean has_win_or_victory = false;
                    for(String s: config.getStringList("Eventos.List")) {

                        String[] separated = s.split(":");
                        if(EventoConfigFile.exists(separated[0])) {

                            Map<String, Integer> player_wins = aEventos.getCacheManager().getPlayerWins(p);
                            Map<String, Integer> player_participations = aEventos.getCacheManager().getPlayerParticipations(p);

                            int wins = 0;
                            if(player_wins != null && player_wins.containsKey(separated[0])) wins = player_wins.get(separated[0]);

                            int participations = 0;
                            if(player_participations != null && player_participations.containsKey(separated[0])) participations = player_participations.get(separated[0]);

                            if(config.getBoolean("Eventos.Only with wins") && wins == 0 && participations == 0) continue;
                            has_win_or_victory = true;
                            lore.add(config.getString("Eventos.Format").replace("@evento_name", separated[1]).replace("@evento_wins", String.valueOf(wins)).replace("@evento_participations", String.valueOf(participations)).replace("&", "§"));

                        }
                    }

                    if(!has_win_or_victory) {
                        lore.add(config.getString("Eventos.Empty").replace("&", "§"));
                    }

                    if(config.getBoolean("Eventos.New line")) lore.add("");

                }

                meta.setLore(lore);
                item.setItemMeta(meta);
                itemSuppliers.add(() -> InventoryItem.of(item));

                position++;
            }

        }

        return itemSuppliers;
    }

    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {

        if(!config.getBoolean("Filter.Enabled")) return;

        AtomicInteger current_filter = new AtomicInteger(player_filter.getOrDefault(viewer.getName(), -1));

        List<String> lore = new ArrayList<>();

        ItemStack is = XMaterial.HOPPER.parseItem();
        assert is != null;
        ItemMeta meta = is.getItemMeta();

        meta.setDisplayName("§6Filtro de ranking");

        lore.add("§7Selecione qual ranking você quer ver.");
        lore.add("");
        lore.add(getFilterFormating(current_filter.get(), -1) + " Vitórias");
        lore.add(getFilterFormating(current_filter.get(), 0) + " Participações");

        lore.add("");
        lore.add("§aClique para mudar o filtro!");

        meta.setLore(lore);

        is.setItemMeta(meta);
        editor.setItem(40, InventoryItem.of(is)
                .defaultCallback(event -> {
                    player_filter.put(viewer.getName(), current_filter.incrementAndGet() > 0 ? -1 : current_filter.get());
                    event.updateInventory();
                }));
    }

    @Override
    protected void update(Viewer viewer, InventoryEditor editor) {
        super.update(viewer, editor);
        configureViewer(viewer);
        configureInventory(viewer, viewer.getEditor());
    }

    private String getFilterFormating(int currentFilter, int loopFilter) {
        return currentFilter == loopFilter ? " §b▶" : "§8";
    }

    public void updateConfig() {
        config = MenuConfigFile.get("top_players");
    }

}