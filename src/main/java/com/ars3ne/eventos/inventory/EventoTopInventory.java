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

        // Vitórias.
        if(current_filter.get() == -1) {

            for(OfflinePlayer p: aEventos.getCacheManager().getTopWinsMenuItems().keySet()) {

                if(p == null) continue;
                if(p.getName() == null) continue;

                ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
                assert item != null;
                SkullMeta meta = (SkullMeta) item.getItemMeta();

                meta.setOwner(p.getName());
                meta.setDisplayName(config.getString("Menu.Items.Player.Name").replace("@top_player", p.getName()).replace("&", "§"));

                meta.setLore(aEventos.getCacheManager().getTopWinsMenuItems().get(p));
                item.setItemMeta(meta);
                itemSuppliers.add(() -> InventoryItem.of(item));

            }

        }

        // Participações.
        if(current_filter.get() == 0) {

            for (OfflinePlayer p : aEventos.getCacheManager().getTopParticipations().keySet()) {

                if(p == null) continue;
                if(p.getName() == null) continue;

                ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
                assert item != null;
                SkullMeta meta = (SkullMeta) item.getItemMeta();

                meta.setOwner(p.getName());
                meta.setDisplayName(config.getString("Menu.Items.Player.Name").replace("@top_player", p.getName()).replace("&", "§"));

                meta.setLore(aEventos.getCacheManager().getTopParticipations().get(p));
                item.setItemMeta(meta);
                itemSuppliers.add(() -> InventoryItem.of(item));

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