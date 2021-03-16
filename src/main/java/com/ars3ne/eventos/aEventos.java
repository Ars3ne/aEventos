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

package com.ars3ne.eventos;

import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.commands.EventoCommand;
import com.ars3ne.eventos.hooks.LegendChatHook;
import com.ars3ne.eventos.listeners.EventoListener;
import com.ars3ne.eventos.manager.AutoStarter;
import com.ars3ne.eventos.manager.ConnectionManager;
import com.ars3ne.eventos.manager.EventosManager;
import com.ars3ne.eventos.utils.ConfigFile;
import com.ars3ne.eventos.manager.TagManager;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class aEventos extends JavaPlugin {

    private static final ConnectionManager connection = new ConnectionManager();
    private static final EventosManager manager = new EventosManager();
    private static final TagManager tag_manager = new TagManager();
    private final AutoStarter autostart = new AutoStarter();

    private SimpleClans clan = null;
    private static final LegendChatHook lc_hook = new LegendChatHook();
    private final EventoListener setup_listener = new EventoListener();

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aIniciando plugin...");

        setupConfig();
        if(connection.setup()) {

            setupListener();
            tag_manager.setup();
            setupAddons();

            autostart.setup();

            this.getCommand("evento").setExecutor(new EventoCommand());
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aPlugin iniciado com sucesso!");
        }

    }

    @Override
    public void onDisable() {

        // Se estiver ocorrendo um evento, então o cancele.
        if(manager.getEvento() != null) {
            manager.getEvento().stop();
            manager.startEvento(EventoType.NONE, null);
        }

        removeListeners();
        autostart.stop();
        connection.close();

        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cPlugin desativado com sucesso!");
    }

    private void setupConfig() {

        // Se a config.yml não existe, então crie as configurações padrões dos eventos.
        File settings = new File(aEventos.getInstance().getDataFolder() + "/config.yml");
        if(!settings.exists()) {
            ConfigFile.create("parkour");
            ConfigFile.create("campominado");
            ConfigFile.create("spleef");
            ConfigFile.create("corrida");
            ConfigFile.create("semaforo");
            ConfigFile.create("batataquente");
            ConfigFile.create("frog");
            ConfigFile.create("fight");
            ConfigFile.create("killer");
            ConfigFile.create("sumo");
            ConfigFile.create("astronauta");
            ConfigFile.create("paintball");
        }

        this.saveDefaultConfig();
    }

    private void setupListener() {
        getServer().getPluginManager().registerEvents(setup_listener, this);
    }

    private void removeListeners() {
        HandlerList.unregisterAll(setup_listener);
        HandlerList.unregisterAll(lc_hook);
    }

    private void setupAddons() {
        if(!setupSimpleClans()) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cSimpleClans não encontrado.");
        }
        if(!setupLegendChat()) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cLegendChat não encontrado.");
        }
    }

    private boolean setupLegendChat() {
        try {
            Class.forName("br.com.devpaulo.legendchat.api.events.ChatMessageEvent");
            getServer().getPluginManager().registerEvents(lc_hook, this);
            return true;
        }catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean setupSimpleClans() {
        Plugin simpleclans = getServer().getPluginManager().getPlugin("SimpleClans");
        if(simpleclans == null) return false;
        clan = ((SimpleClans) simpleclans);
        return true;
    }

    public static EventosManager getEventoManager() {
        return manager;
    }

    public SimpleClans getSimpleClans() {
        return this.clan;
    }

    public static aEventos getInstance() {
        return (aEventos) Bukkit.getServer().getPluginManager().getPlugin("aEventos");
    }

    public static ConnectionManager getConnectionManager() {
        return connection;
    }
    public static TagManager getTagManager() {
        return tag_manager;
    }

    public static void updateTags() {
        tag_manager.setup();
        lc_hook.updateTags();
    }

}