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
import com.ars3ne.eventos.hooks.BungeecordHook;
import com.ars3ne.eventos.hooks.LegendChatHook;
import com.ars3ne.eventos.hooks.PlaceholderAPIHook;
import com.ars3ne.eventos.listeners.EventoListener;
import com.ars3ne.eventos.manager.*;
import com.ars3ne.eventos.utils.ConfigUpdater;
import com.ars3ne.eventos.utils.EventoConfigFile;
import com.ars3ne.eventos.utils.MenuConfigFile;
import com.henryfabio.minecraft.inventoryapi.manager.InventoryManager;
import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class aEventos extends JavaPlugin {

    private static final ConnectionManager connection = new ConnectionManager();
    private static final EventosManager manager = new EventosManager();
    private static final EventosChatManager chat_manager = new EventosChatManager();
    private static final TagManager tag_manager = new TagManager();
    private static final CacheManager cache = new CacheManager();
    private final AutoStarter autostart = new AutoStarter();

    private SimpleClans clan = null;
    private static final LegendChatHook lc_hook = new LegendChatHook();
    private Economy econ = null;

    private boolean hooked_massivefactions = false;
    private boolean hooked_yclans = false;
    private boolean is_reloaded = true;

    private final EventoListener setup_listener = new EventoListener();

    @Override
    public void onLoad() {
        is_reloaded = false;
    }

    @Override
    public void onEnable() {

        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aIniciando plugin...");

        // Se o plugin foi recarregado, mande um aviso no console.
        if(is_reloaded) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cEste plugin não é compatível com o /reload, nem com o Plugman ou plugins parecidos.");
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cVários erros podem ocorrer e você não receberá suporte. Caso queira recarregar os arquivos de configuração, use o comando /evento reload.");
        }

        setupConfig();
        if(connection.setup()) {

            if(conversor()) return;

            if(getConfig().getBoolean("UpdateChecker")) {
                UpdateChecker.verify();
            }

            setupListener();
            tag_manager.setup();
            setupAddons();

            autostart.setup();
            cache.updateCache();
            InventoryManager.enable(this);

            // Se tem suporte para o Bungeecord, então registre os canais.
            if(getConfig().getBoolean("Bungeecord.Enabled")) {
                this.getServer().getMessenger().registerOutgoingPluginChannel(this, "aeventos:channel");
                this.getServer().getMessenger().registerIncomingPluginChannel(this, "aeventos:channel", new BungeecordHook());
            }

            this.getCommand("evento").setExecutor(new EventoCommand());
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aPlugin iniciado com sucesso!");
        }

    }

    @Override
    public void onDisable() {

        is_reloaded = false;

        // Se estiver ocorrendo um evento, então o cancele.
        if(manager.getEvento() != null) {
            manager.getEvento().stop();
            manager.startEvento(EventoType.NONE, null);
        }

        if(chat_manager.getEvento() != null) {
            chat_manager.getEvento().stop();
            chat_manager.startEvento(EventoType.NONE, null);
        }

        removeListeners();
        autostart.stop();
        connection.close();
        Bukkit.getServer().getScheduler().cancelTasks(this);

        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cPlugin desativado com sucesso!");
    }

    private void setupConfig() {

        // Se a config.yml não existe, então crie as configurações padrões dos eventos.
        File settings = new File(aEventos.getInstance().getDataFolder() + "/config.yml");

        if(!settings.exists()) {

            // Configurações de Eventos
            EventoConfigFile.create("parkour");
            EventoConfigFile.create("campominado");
            EventoConfigFile.create("spleef");
            EventoConfigFile.create("corrida");
            EventoConfigFile.create("semaforo");
            EventoConfigFile.create("batataquente");
            EventoConfigFile.create("frog");
            EventoConfigFile.create("fight");
            EventoConfigFile.create("killer");
            EventoConfigFile.create("sumo");
            EventoConfigFile.create("astronauta");
            EventoConfigFile.create("paintball");
            EventoConfigFile.create("votacao");
            EventoConfigFile.create("hunter");
            EventoConfigFile.create("quiz");
            EventoConfigFile.create("anvil");
            EventoConfigFile.create("loteria");
            EventoConfigFile.create("bolao");
            EventoConfigFile.create("gladiador");
            EventoConfigFile.create("matematica");
            EventoConfigFile.create("palavra");
            EventoConfigFile.create("fastclick");
            EventoConfigFile.create("nexus");
            EventoConfigFile.create("sorteio");

        }

        // Se o arquivo menus/main.yml não existe, então crie os arquivos de configuração dos menus.
        File gui_settings = new File(aEventos.getInstance().getDataFolder() + "/menus/main.yml");

        if(!gui_settings.exists()) {

            // Configurações da GUI
            MenuConfigFile.create("main");
            MenuConfigFile.create("eventos");
            MenuConfigFile.create("top_players");

        }

        this.saveDefaultConfig();

        // Tenta atualizar o arquivo de configuração.
        try {
            ConfigUpdater.update(this, "config.yml", settings, Collections.singletonList("none"));
            ConfigUpdater.update(this, "menus/main.yml", new File(aEventos.getInstance().getDataFolder() + "/menus/main.yml"), Collections.singletonList("none"));
            ConfigUpdater.update(this, "menus/top_players.yml", new File(aEventos.getInstance().getDataFolder() + "/menus/top_players.yml"), Collections.singletonList("none"));

        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível atualizar o arquivo de configuração.");
            e.printStackTrace();
        }

        // Tenta atualizar os eventos.
        ConfigUpdater.updateEventos();

    }

    private boolean conversor() {

        // Caso o conversor esteja ativado, então converta as informações do plugin.
        if(getConfig().getBoolean("Conversor.Enabled")) {

            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aIniciando conversão...");

            // Se a database de usuários do aEventos não estiver vazia, desligue o plugin.
            if(!getConnectionManager().isEmpty()) {
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cA database do aEventos não está vazia! Desativando plugin...");
                getPluginLoader().disablePlugin(this);
                return true;
            }

            // Faça a conversão.

            ConversorConnectionManager conversor = new ConversorConnectionManager();
            conversor.setup();

            boolean converted;

            switch(getConfig().getString("Conversor.Plugin").toLowerCase()) {
                case "heventos":
                    converted = conversor.convertHEventos();
                    if(!converted) return true;
                    break;
                case "yeventos":
                    converted = conversor.convertyEventos();
                    if(!converted) return true;
                    break;
                default:
                    Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cConversor para o plugin '" + getConfig().getString("Conversor.Plugin") + "' não encontrado. Desativando plugin...");
                    getPluginLoader().disablePlugin(this);
                    return true;
            }

            // A conversão foi feita com sucesso. Desative o plugin.
            conversor.close();
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConversão feita com sucesso! Desative o modo conversão para iniciar o plugin.");
            getPluginLoader().disablePlugin(this);
            return true;
        }

        return false;

    }

    private void setupListener() {
        getServer().getPluginManager().registerEvents(setup_listener, this);
    }

    private void removeListeners() {
        HandlerList.unregisterAll(setup_listener);
        HandlerList.unregisterAll(lc_hook);
    }

    private void setupAddons() {
        if(!setupSimpleClans() && !setupMassiveFactions() && !setupyClans()) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cSimpleClans, MassiveFactions e yClans não encontrados.");
        }
        if(!setupLegendChat()) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cLegendChat não encontrado.");
        }
        if(!setupEconomy()) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cVault não encontrado.");
        }
        if(!setupPlaceholderAPI()) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cPlaceholderAPI não encontrado.");
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

    private boolean setupMassiveFactions() {
        Plugin factions = getServer().getPluginManager().getPlugin("Factions");
        if(factions == null) return false;
        hooked_massivefactions = true;
        return true;
    }

    private boolean setupyClans() {
        Plugin yclans = getServer().getPluginManager().getPlugin("yClans");
        if(yclans == null) return false;
        hooked_yclans = true;
        return true;
    }

    private boolean setupPlaceholderAPI() {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PlaceholderAPIHook(this).register();
            return true;
        }
        return false;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public static EventosManager getEventoManager() {
        return manager;
    }

    public static EventosChatManager getEventoChatManager() {
        return chat_manager;
    }

    public SimpleClans getSimpleClans() {
        return this.clan;
    }

    public Economy getEconomy() { return this.econ; }

    public static CacheManager getCacheManager() { return cache; }

    public boolean isHookedMassiveFactions() { return this.hooked_massivefactions; }

    public boolean isHookedyClans() { return this.hooked_yclans; }

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
        cache.updateCache();
    }

}
