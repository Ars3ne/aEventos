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

package com.ars3ne.eventos.manager;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.EventoType;
import com.ars3ne.eventos.utils.EventoConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class TagManager {

    private static final Map<String, String> lc_tags = new HashMap<>();
    private static final Map<OfflinePlayer, List<String>> lc_tag_holders = new HashMap<>();

    public void setup() {
        // Não estou orgulhoso dessa gambiarra, mas é o que temos para hoje.
        // Leia todos os arquivos de configuração de eventos e adicione as suas tags a lista.
        for (File file : Objects.requireNonNull(EventoConfigFile.getAllFiles())) {

            if(file.getName().contains("old")) continue;
            // Adicione o evento á database.
            YamlConfiguration config = EventoConfigFile.get(file.getName().substring(0, file.getName().length() - 4));

            if (config.getConfigurationSection("Rewards.Tag") == null) continue;
            if (!config.getBoolean("Rewards.Tag.Enabled")) continue;

            lc_tags.put(config.getString("Rewards.Tag.Name"), config.getString("Rewards.Tag.Style").replace("&", "§"));

            String holder;

            // Se o evento for de clans, então obtenha os vencedores do evento.
            if(EventoType.isEventoGuild(EventoType.getEventoType(config.getString("Evento.Type")))) {
                aEventos.getConnectionManager().createEventoGuild(file.getName().substring(0, file.getName().length() - 4));
                holder = aEventos.getConnectionManager().getEventoGuildWinners(file.getName().substring(0, file.getName().length() - 4)).replace("[", "").replace("]", "");
            }else {
                aEventos.getConnectionManager().createEvento(file.getName().substring(0, file.getName().length() - 4));
                holder = aEventos.getConnectionManager().getEventoWinners(file.getName().substring(0, file.getName().length() - 4)).replace("[", "").replace("]", "");
            }

            for (String uuid : holder.split(", ")) {

                if (uuid.isEmpty()) continue;
                if (!lc_tag_holders.containsKey(aEventos.getInstance().getServer().getOfflinePlayer(UUID.fromString(uuid)))) {
                    lc_tag_holders.put(Bukkit.getOfflinePlayer(UUID.fromString(uuid)), new ArrayList<>());
                }
                List<String> tags = lc_tag_holders.get(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
                tags.add(config.getString("Rewards.Tag.Name"));
                lc_tag_holders.put(Bukkit.getOfflinePlayer(UUID.fromString(uuid)), tags);
            }
        }
    }

    public void updateTagHolder(YamlConfiguration config) {

        lc_tag_holders.clear();
        String holder = aEventos.getConnectionManager().getEventoWinners(config.getString("filename").substring(0, config.getString("filename").length() - 4)).replace("[", "").replace("]", "");
        for (String uuid : holder.split(", ")) {

            if (uuid.isEmpty()) continue;

            if (!lc_tag_holders.containsKey(aEventos.getInstance().getServer().getOfflinePlayer(UUID.fromString(uuid)))) {
                lc_tag_holders.put(Bukkit.getOfflinePlayer(UUID.fromString(uuid)), new ArrayList<>());
            }

            List<String> tags = lc_tag_holders.get(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
            tags.add(config.getString("Rewards.Tag.Name"));
            lc_tag_holders.put(Bukkit.getOfflinePlayer(UUID.fromString(uuid)), tags);
        }
    }

    public Map<String, String> getTags() {
        return lc_tags;
    }
    public Map<OfflinePlayer, List<String>> getTagHolders() {
        return lc_tag_holders;
    }

}
