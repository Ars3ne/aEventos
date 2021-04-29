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

import com.ars3ne.eventos.inventory.utils.SimpleItemParser;
import com.ars3ne.eventos.manager.InventoryManager;
import com.ars3ne.eventos.utils.MenuConfigFile;
import com.henryfabio.minecraft.inventoryapi.editor.InventoryEditor;
import com.henryfabio.minecraft.inventoryapi.inventory.impl.paged.PagedInventory;
import com.henryfabio.minecraft.inventoryapi.item.InventoryItem;
import com.henryfabio.minecraft.inventoryapi.item.supplier.InventoryItemSupplier;
import com.henryfabio.minecraft.inventoryapi.viewer.Viewer;
import com.henryfabio.minecraft.inventoryapi.viewer.configuration.impl.ViewerConfigurationImpl;
import com.henryfabio.minecraft.inventoryapi.viewer.impl.paged.PagedViewer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedList;
import java.util.List;

public final class EventoListInventory extends PagedInventory {

    private YamlConfiguration config = MenuConfigFile.get("eventos");

    public EventoListInventory() {
        super(
                "aeventos.inventory.eventolist",
                "&8Eventos",
                27
        );

    }

    @Override
    protected void configureViewer(PagedViewer viewer) {
        ViewerConfigurationImpl.Paged configuration = viewer.getConfiguration();
        configuration.titleInventory(config.getString("Menu.Name").replace("@jogador", viewer.getName()).replace("&", "§"));
        configuration.inventorySize(config.getInt("Menu.Size"));
        configuration.nextPageSlot(config.getInt("Menu.Next page slot"));
        configuration.previousPageSlot(config.getInt("Menu.Previous page slot"));
        configuration.emptyPageSlot(config.getInt("Menu.Empty page slot"));
        configuration.itemPageLimit(config.getInt("Menu.Item page limit"));
    }

    @Override
    protected List<InventoryItemSupplier> createPageItems(PagedViewer viewer) {

        List<InventoryItemSupplier> itemSuppliers = new LinkedList<>();

        for(String key: config.getConfigurationSection("Menu.Items.Eventos").getKeys(false)) {
            InventoryItem item = InventoryItem.of(SimpleItemParser.parse(config.getConfigurationSection("Menu.Items.Eventos." + key), null));
            itemSuppliers.add(() -> item);
        }

        return itemSuppliers;
    }

    @Override
    protected void configureInventory(Viewer viewer, InventoryEditor editor) {

        // Se o item de voltar estiver ativo, então o adicione.
        if(config.getBoolean("Menu.Items.Back.Enabled")) {
            InventoryItem item = InventoryItem.of(SimpleItemParser.parse(config.getConfigurationSection("Menu.Items.Back"), null));
            item.defaultCallback(event -> InventoryManager.openMainInventory(viewer.getPlayer()) );
            editor.setItem(config.getInt("Menu.Items.Back.Slot"), item);
        }

    }

    public void updateConfig() {
        config = MenuConfigFile.get("eventos");
    }

}