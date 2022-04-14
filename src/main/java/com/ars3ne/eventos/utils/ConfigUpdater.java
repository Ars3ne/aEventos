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

import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.utils.converters.config.LegacySerializerConverter;
import com.ars3ne.eventos.utils.converters.config.SerializerConverter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class to update/add new sections/keys to your config while keeping your current values and keeping your comments
 * Algorithm:
 * Read the new file and scan for comments and ignored sections, if ignored section is found it is treated as a comment.
 * Read and write each line of the new config, if the old config has value for the given key it writes that value in the new config.
 * If a key has an attached comment above it, it is written first.
 * @author tchristofferson
 */
public class ConfigUpdater {

    /**
     * Update a yaml file from a resource inside your plugin jar
     * @param plugin You plugin
     * @param resourceName The yaml file name to update from, typically config.yml
     * @param toUpdate The yaml file to update
     * @param ignoredSections List of sections to ignore and copy from the current config
     * @throws IOException If an IOException occurs
     */
    public static void update(Plugin plugin, String resourceName, File toUpdate, List<String> ignoredSections) throws IOException {
        BufferedReader newReader = new BufferedReader(new InputStreamReader(plugin.getResource(resourceName), StandardCharsets.UTF_8));
        List<String> newLines = newReader.lines().collect(Collectors.toList());
        newReader.close();

        FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(toUpdate);
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(resourceName)));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(toUpdate), StandardCharsets.UTF_8));

        List<String> ignoredSectionsArrayList = new ArrayList<>(ignoredSections);
        //ignoredSections can ONLY contain configurations sections
        ignoredSectionsArrayList.removeIf(ignoredSection -> !newConfig.isConfigurationSection(ignoredSection));

        Yaml yaml = new Yaml();
        Map<String, String> comments = parseComments(newLines, ignoredSectionsArrayList, oldConfig, yaml);
        write(newConfig, oldConfig, comments, ignoredSectionsArrayList, writer, yaml);
    }

    //Write method doing the work.
    //It checks if key has a comment associated with it and writes comment then the key and value
    private static void write(FileConfiguration newConfig, FileConfiguration oldConfig, Map<String, String> comments, List<String> ignoredSections, BufferedWriter writer, Yaml yaml) throws IOException {
        outer: for (String key : newConfig.getKeys(true)) {
            String[] keys = key.split("\\.");
            String actualKey = keys[keys.length - 1];
            String comment = comments.remove(key);

            StringBuilder prefixBuilder = new StringBuilder();
            int indents = keys.length - 1;
            appendPrefixSpaces(prefixBuilder, indents);
            String prefixSpaces = prefixBuilder.toString();

            if (comment != null) {
                writer.write(comment);//No \n character necessary, new line is automatically at end of comment
            }

            for (String ignoredSection : ignoredSections) {
                if (key.startsWith(ignoredSection)) {
                    continue outer;
                }
            }

            Object newObj = newConfig.get(key);
            Object oldObj = oldConfig.get(key);

            if (newObj instanceof ConfigurationSection && oldObj instanceof ConfigurationSection) {
                //write the old section
                writeSection(writer, actualKey, prefixSpaces, (ConfigurationSection) oldObj);
            } else if (newObj instanceof ConfigurationSection) {
                //write the new section, old value is no more
                writeSection(writer, actualKey, prefixSpaces, (ConfigurationSection) newObj);
            } else if (oldObj != null) {
                //write the old object
                write(oldObj, actualKey, prefixSpaces, yaml, writer);
            } else {
                //write new object
                write(newObj, actualKey, prefixSpaces, yaml, writer);
            }
        }

        String danglingComments = comments.get(null);

        if (danglingComments != null) {
            writer.write(danglingComments);
        }

        writer.close();
    }

    //Doesn't work with configuration sections, must be an actual object
    //Auto checks if it is serializable and writes to file
    private static void write(Object obj, String actualKey, String prefixSpaces, Yaml yaml, BufferedWriter writer) throws IOException {
        if (obj instanceof ConfigurationSerializable) {
            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(((ConfigurationSerializable) obj).serialize()));
        } else if (obj instanceof String || obj instanceof Character) {
            if (obj instanceof String) {
                String s = (String) obj;
                obj = s.replace("\n", "\\n");
            }

            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(obj));
        } else if (obj instanceof List) {
            writeList((List) obj, actualKey, prefixSpaces, yaml, writer);
        } else {
            writer.write(prefixSpaces + actualKey + ": " + yaml.dump(obj));
        }
    }

    //Writes a configuration section
    private static void writeSection(BufferedWriter writer, String actualKey, String prefixSpaces, ConfigurationSection section) throws IOException {
        if (section.getKeys(false).isEmpty()) {
            writer.write(prefixSpaces + actualKey + ": {}");
        } else {
            writer.write(prefixSpaces + actualKey + ":");
        }

        writer.write("\n");
    }

    //Writes a list of any object
    private static void writeList(List list, String actualKey, String prefixSpaces, Yaml yaml, BufferedWriter writer) throws IOException {
        writer.write(getListAsString(list, actualKey, prefixSpaces, yaml));
    }

    private static String getListAsString(List list, String actualKey, String prefixSpaces, Yaml yaml) {
        StringBuilder builder = new StringBuilder(prefixSpaces).append(actualKey).append(":");

        if (list.isEmpty()) {
            builder.append(" []\n");
            return builder.toString();
        }

        builder.append("\n");

        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);

            if (o instanceof String || o instanceof Character) {
                builder.append(prefixSpaces).append("- '").append(o).append("'");
            } else if (o instanceof List) {
                builder.append(prefixSpaces).append("- ").append(yaml.dump(o));
            } else {
                builder.append(prefixSpaces).append("- ").append(o);
            }

            if (i != list.size()) {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    //Key is the config key, value = comment and/or ignored sections
    //Parses comments, blank lines, and ignored sections
    private static Map<String, String> parseComments(List<String> lines, List<String> ignoredSections, FileConfiguration oldConfig, Yaml yaml) {
        Map<String, String> comments = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        int lastLineIndentCount = 0;

        outer: for (String line : lines) {
            if (line != null && line.trim().startsWith("-"))
                continue;

            if (line == null || line.trim().equals("") || line.trim().startsWith("#")) {
                builder.append(line).append("\n");
            } else {
                lastLineIndentCount = setFullKey(keyBuilder, line, lastLineIndentCount);

                for (String ignoredSection : ignoredSections) {
                    if (keyBuilder.toString().equals(ignoredSection)) {
                        Object value = oldConfig.get(keyBuilder.toString());

                        if (value instanceof ConfigurationSection)
                            appendSection(builder, (ConfigurationSection) value, new StringBuilder(getPrefixSpaces(lastLineIndentCount)), yaml);

                        continue outer;
                    }
                }

                if (keyBuilder.length() > 0) {
                    comments.put(keyBuilder.toString(), builder.toString());
                    builder.setLength(0);
                }
            }
        }

        if (builder.length() > 0) {
            comments.put(null, builder.toString());
        }

        return comments;
    }

    private static void appendSection(StringBuilder builder, ConfigurationSection section, StringBuilder prefixSpaces, Yaml yaml) {
        builder.append(prefixSpaces).append(getKeyFromFullKey(section.getCurrentPath())).append(":");
        Set<String> keys = section.getKeys(false);

        if (keys.isEmpty()) {
            builder.append(" {}\n");
            return;
        }

        builder.append("\n");
        prefixSpaces.append("  ");

        for (String key : keys) {
            Object value = section.get(key);
            String actualKey = getKeyFromFullKey(key);

            if (value instanceof ConfigurationSection) {
                appendSection(builder, (ConfigurationSection) value, prefixSpaces, yaml);
                prefixSpaces.setLength(prefixSpaces.length() - 2);
            } else if (value instanceof List) {
                builder.append(getListAsString((List) value, actualKey, prefixSpaces.toString(), yaml));
            } else {
                builder.append(prefixSpaces).append(actualKey).append(": ").append(yaml.dump(value));
            }
        }
    }

    //Counts spaces in front of key and divides by 2 since 1 indent = 2 spaces
    private static int countIndents(String s) {
        int spaces = 0;

        for (char c : s.toCharArray()) {
            if (c == ' ') {
                spaces += 1;
            } else {
                break;
            }
        }

        return spaces / 2;
    }

    //Ex. keyBuilder = key1.key2.key3 --> key1.key2
    private static void removeLastKey(StringBuilder keyBuilder) {
        String temp = keyBuilder.toString();
        String[] keys = temp.split("\\.");

        if (keys.length == 1) {
            keyBuilder.setLength(0);
            return;
        }

        temp = temp.substring(0, temp.length() - keys[keys.length - 1].length() - 1);
        keyBuilder.setLength(temp.length());
    }

    private static String getKeyFromFullKey(String fullKey) {
        String[] keys = fullKey.split("\\.");
        return keys[keys.length - 1];
    }

    //Updates the keyBuilder and returns configLines number of indents
    private static int setFullKey(StringBuilder keyBuilder, String configLine, int lastLineIndentCount) {
        int currentIndents = countIndents(configLine);
        String key = configLine.trim().split(":")[0];

        if (keyBuilder.length() == 0) {
            keyBuilder.append(key);
        } else if (currentIndents == lastLineIndentCount) {
            //Replace the last part of the key with current key
            removeLastKey(keyBuilder);

            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }

            keyBuilder.append(key);
        } else if (currentIndents > lastLineIndentCount) {
            //Append current key to the keyBuilder
            keyBuilder.append(".").append(key);
        } else {
            int difference = lastLineIndentCount - currentIndents;

            for (int i = 0; i < difference + 1; i++) {
                removeLastKey(keyBuilder);
            }

            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }

            keyBuilder.append(key);
        }

        return currentIndents;
    }

    private static String getPrefixSpaces(int indents) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < indents; i++) {
            builder.append("  ");
        }

        return builder.toString();
    }

    private static void appendPrefixSpaces(StringBuilder builder, int indents) {
        builder.append(getPrefixSpaces(indents));
    }

    // Atualiza as configurações dos eventos.
    public static void updateEventos() {

        for (File file : Objects.requireNonNull(EventoConfigFile.getAllFiles())) {

            if(file.getName().contains("old")) continue;
            YamlConfiguration config = EventoConfigFile.get(file.getName().substring(0, file.getName().length() - 4));

            if(config.getString("Evento.Type") == null) continue;

            // Se a opção "Server" não existir nas localizações então a adicione.
            if(config.getString("Locations.Server") == null) {
                if(config.getConfigurationSection("Locations") == null) continue;
                config.set("Locations.Server", "null");
                try {
                    EventoConfigFile.save(config);
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível atualizar o arquivo de configuração.");
                    e.printStackTrace();
                }
            }

            // Se o evento for de chat e a configuração da variável de recompensa não existir, adicione-a.
            if(!EventoType.isEventoChat(EventoType.getEventoType(config.getString("Evento.Type")))) {
                if (config.isSet("Rewards.Money")) continue;
                config.set("Rewards.Money", 1000);
                try {
                    EventoConfigFile.save(config);
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível atualizar o arquivo de configuração.");
                    e.printStackTrace();
                }
            }

            // Converter itens.

            switch(EventoType.getEventoType(config.getString("Evento.Type"))) {

                case FIGHT:
                    LegacySerializerConverter.convertFight(config);
                    SerializerConverter.convert(config);
                    continue;
                case SPLEEF:
                    LegacySerializerConverter.convertSpleef(config);
                    SerializerConverter.convert(config);
                    continue;
                case KILLER:
                    // TODO: Talvez mudar isso para uma classe propria?
                    // Adicione a seção dos itens padrões.
                    if(config.getConfigurationSection("Itens") == null) {

                        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConvertendo o arquivo de configuração §f" + config.getString("filename") + " §apara a nova versão...");

                        // Armadura padrão.
                        config.set("Itens.Enabled", false);

                        // Capacete
                        config.set("Itens.Helmet.material", "DIAMOND_HELMET");
                        config.set("Itens.Helmet.damage", 0);
                        config.set("Itens.Helmet.enchants.PROTECTION_ENVIRONMENTAL", 4);
                        config.set("Itens.Helmet.enchants.DURABILITY", 4);

                        // Peitoral
                        config.set("Itens.Chestplate.material", "DIAMOND_CHESTPLATE");
                        config.set("Itens.Chestplate.damage", 0);
                        config.set("Itens.Chestplate.enchants.PROTECTION_ENVIRONMENTAL", 4);
                        config.set("Itens.Chestplate.enchants.DURABILITY", 4);

                        // Calças
                        config.set("Itens.Leggings.material", "DIAMOND_LEGGINGS");
                        config.set("Itens.Leggings.damage", 0);
                        config.set("Itens.Leggings.enchants.PROTECTION_ENVIRONMENTAL", 4);
                        config.set("Itens.Leggings.enchants.DURABILITY", 4);

                        // Botas
                        config.set("Itens.Boots.material", "DIAMOND_BOOTS");
                        config.set("Itens.Boots.damage", 0);
                        config.set("Itens.Boots.enchants.PROTECTION_ENVIRONMENTAL", 4);
                        config.set("Itens.Boots.enchants.DURABILITY", 4);

                        // Itens padrões do inventário.
                        config.set("Itens.Inventory.0.material", "DIAMOND_SWORD");
                        config.set("Itens.Inventory.0.damage", 0);
                        config.set("Itens.Inventory.0.enchants.DAMAGE_ALL", 5);

                        config.set("Itens.Inventory.1.material", "GOLDEN_APPLE");
                        config.set("Itens.Inventory.1.amount", 64);
                        config.set("Itens.Inventory.1.damage", 1);

                        config.set("Itens.Inventory.2.material", "POTION");
                        config.set("Itens.Inventory.2.damage", 41);
                        config.set("Itens.Inventory.2.name", "&5Poção de Força");
                        config.set("Itens.Inventory.2.level", 2);
                        config.set("Itens.Inventory.2.base-effect", "STRENGTH, false, false");

                        try {
                            EventoConfigFile.save(config);
                            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aArquivo §f" + config.getString("filename") + " §aconvertido com sucesso!");
                        } catch (IOException e) {
                            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível converter o arquivo de configuração.");
                            e.printStackTrace();
                        }

                    }
                    continue;
                case SUMO:
                    // Adicione a seção dos itens padrões.
                    if(config.getConfigurationSection("Itens") == null) {

                        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConvertendo o arquivo de configuração §f" + config.getString("filename") + " §apara a nova versão...");

                        // Armadura padrão.
                        config.set("Itens.Enabled", true);

                        // Capacete
                        config.set("Itens.Helmet", null);
                        config.set("Itens.Helmet.a", "shaark");
                        config.set("Itens.Helmet.a", null);

                        // Peitoral
                        config.set("Itens.Chestplate", null);
                        config.set("Itens.Chestplate.a", "shaark");
                        config.set("Itens.Chestplate.a", null);

                        // Calças
                        config.set("Itens.Leggings", null);
                        config.set("Itens.Leggings.a", "shaark");
                        config.set("Itens.Leggings.a", null);

                        // Botas
                        config.set("Itens.Boots", null);
                        config.set("Itens.Boots.a", "shaark");
                        config.set("Itens.Boots.a", null);

                        // Itens padrões do inventário.
                        config.set("Itens.Inventory.0.material", "STICK");
                        config.set("Itens.Inventory.0.damage", 0);
                        config.set("Itens.Inventory.0.enchants.KNOCKBACK", 5);

                        try {
                            EventoConfigFile.save(config);
                            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aArquivo §f" + config.getString("filename") + " §aconvertido com sucesso!");
                        } catch (IOException e) {
                            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível converter o arquivo de configuração.");
                            e.printStackTrace();
                        }

                    }
                    continue;
                 case HUNTER:

                    // Defina a configuração de invencibilidade, caso não esteja definida.
                    if(!config.isSet("Evento.Invincibility")) {

                        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConvertendo o arquivo de configuração §f" + config.getString("filename") + " §apara a nova versão...");

                        config.set("Evento.Invincibility", 5);

                        try {
                            EventoConfigFile.save(config);
                            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aArquivo §f" + config.getString("filename") + " §aconvertido com sucesso!");
                        } catch (IOException e) {
                            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível converter o arquivo de configuração.");
                            e.printStackTrace();
                        }

                    }
                    continue;
                case CAMPO_MINADO:

                    if(config.isSet("Evento.Last player win")) break;

                    Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aConvertendo o arquivo de configuração §f" + config.getString("filename") + " §apara a nova versão...");

                    config.set("Evento.Last player win", false);

                    try {
                        EventoConfigFile.save(config);
                        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aArquivo §f" + config.getString("filename") + " §aconvertido com sucesso!");
                    } catch (IOException e) {
                        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível converter o arquivo de configuração.");
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;

            }

        }

    }

}