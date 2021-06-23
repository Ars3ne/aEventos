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

package com.ars3ne.eventos.utils;

import com.ars3ne.eventos.aEventos;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryConfigFile {

    public static void create(String name) {
        File config_file = new File(aEventos.getInstance().getDataFolder() + "/playerdata/", name + ".yml");
        if (config_file.exists()) return;

        // Se a pasta playerdata não existe, então a crie.
        File directory = new File(aEventos.getInstance().getDataFolder() + "/playerdata/");
        if (! directory.exists()){
            directory.mkdir();
        }

        try {
            config_file.createNewFile();
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

    public static void create(String name, String evento_identifier) {

        File config_file = new File(aEventos.getInstance().getDataFolder() + "/playerdata/backup/" + evento_identifier + "/", name + ".yml");
        if (config_file.exists()) return;

        // Se a pasta backup não existe, então a crie.
        File directory = new File(aEventos.getInstance().getDataFolder() + "/playerdata/backup/");
        if (! directory.exists()){
            directory.mkdir();
        }

        // Se a pasta do evento não existe, então a crie.
        File directory2 = new File(aEventos.getInstance().getDataFolder() + "/playerdata/backup/" + evento_identifier + "/");
        if (! directory2.exists()){
            directory2.mkdir();
        }

        try {
            config_file.createNewFile();
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

    public static YamlConfiguration get(String name) {
        File settings = new File(aEventos.getInstance().getDataFolder() + "/playerdata/", name + ".yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(settings);
        config.set("filename", name + ".yml");

        return config;

    }

    public static boolean exists(String name) {
        File settings = new File(aEventos.getInstance().getDataFolder() + "/playerdata/", name + ".yml");
        return settings.exists();
    }

    public static void save(YamlConfiguration config) throws IOException {
        String filename = config.getString("filename");
        File file = new File(aEventos.getInstance().getDataFolder() + "/playerdata/", filename);
        config.set("filename", null);
        config.save(file);
        config.set("filename", filename);
    }

    public static void save(YamlConfiguration config, String evento_identifier) throws IOException {
        String filename = config.getString("filename");
        File file = new File(aEventos.getInstance().getDataFolder() + "/playerdata/backup/" + evento_identifier, filename);
        config.set("filename", null);
        config.save(file);
        config.set("filename", filename);
    }

    public static void delete(YamlConfiguration config) {
        String filename = config.getString("filename");
        File file = new File(aEventos.getInstance().getDataFolder() + "/playerdata/", filename);
        if(!file.exists()) return;
        file.delete();
    }

    public static void delete(YamlConfiguration config, String evento_identifier) {
        String filename = config.getString("filename");
        File file = new File(aEventos.getInstance().getDataFolder() + "/playerdata/backup/" + evento_identifier + "/", filename);
        if(!file.exists()) return;
        file.delete();
    }

    public static List<File> getAllFiles() {

        try {
            return Files.walk(Paths.get(aEventos.getInstance().getDataFolder() + "/playerdata/"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

}
