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

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigFile {

    public static void create(String name) {
        File config_file = new File(aEventos.getInstance().getDataFolder() + "/eventos/", name + ".yml");

        if (config_file.exists()) return;

        aEventos.getInstance().saveResource("eventos/" + name + ".yml", false);

            /*FileConfiguration config = new YamlConfiguration();
            try {
                config.load(config_file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }*/


    }

    public static YamlConfiguration get(String name) {
        File settings = new File(aEventos.getInstance().getDataFolder() + "/eventos/", name + ".yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(settings);
        config.set("filename", name + ".yml");

        return config;

    }

    public static boolean exists(String name) {
        File settings = new File(aEventos.getInstance().getDataFolder() + "/eventos/", name + ".yml");
        return settings.exists();
    }

    public static void save(YamlConfiguration config) throws IOException {
        String filename = config.getString("filename");
        File file = new File(aEventos.getInstance().getDataFolder() + "/eventos/", filename);
        config.set("filename", null);
        config.save(file);
        config.set("filename", filename);
    }

    public static List<File> getAllFiles() {

        try {

            return Files.walk(Paths.get(aEventos.getInstance().getDataFolder() + "/eventos/"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }

}
