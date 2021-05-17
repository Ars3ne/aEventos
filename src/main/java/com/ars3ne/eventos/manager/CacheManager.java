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
import com.ars3ne.eventos.utils.EventoConfigFile;
import com.ars3ne.eventos.utils.MenuConfigFile;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class CacheManager {

    private final Map<OfflinePlayer, Map<String, Integer>> player_wins = new HashMap<>();
    private final Map<OfflinePlayer, Map<String, Integer>> player_participations = new HashMap<>();

    private LinkedHashMap<OfflinePlayer, Integer> top_player_wins = new LinkedHashMap<>();
    private LinkedHashMap<OfflinePlayer, Integer> top_player_participations = new LinkedHashMap<>();

    private Map<String, String> lc_tags = aEventos.getTagManager().getTags();
    private Map<OfflinePlayer, List<String>> lc_tag_holders = aEventos.getTagManager().getTagHolders();

    private final LinkedHashMap<OfflinePlayer, List<String>> menu_wins_data = new LinkedHashMap<>();
    private final LinkedHashMap<OfflinePlayer, List<String>> menu_participations_data = new LinkedHashMap<>();

    public Map<String, Integer> getPlayerWins(OfflinePlayer p) {
        if(!player_wins.containsKey(p)) return null;
        return player_wins.get(p);
    }

    public Map<String, Integer> getPlayerParticipations(OfflinePlayer p) {
        if(!player_participations.containsKey(p)) return null;
        return player_participations.get(p);
    }

    public void updateCache() {

        if(!aEventos.getInstance().getConfig().getBoolean("Enable GUI")) return;

        player_wins.clear();
        player_participations.clear();
        menu_wins_data.clear();
        menu_participations_data.clear();

        aEventos.getConnectionManager().getPlayersWins();
        aEventos.getConnectionManager().getPlayersParticipations();

    }

    public void updateTags() {
        lc_tags = aEventos.getTagManager().getTags();
        lc_tag_holders = aEventos.getTagManager().getTagHolders();
    }

    public Map<OfflinePlayer, Map<String, Integer>> getPlayerWinsList() {
        return player_wins;
    }

    public Map<OfflinePlayer, Map<String, Integer>> getPlayerParticipationsList() {
        return player_participations;
    }

    public int getPlayerTopWinsPosition(OfflinePlayer p) {

        if(!top_player_wins.containsKey(p)) return 0;

        Set<OfflinePlayer> keys = top_player_wins.keySet();
        List<OfflinePlayer> listKeys = new ArrayList<>( keys );

        return listKeys.indexOf(p) + 1;
    }

    public int getPlayerTopParticipationsPosition(OfflinePlayer p) {

        if(!top_player_participations.containsKey(p)) return 0;

        Set<OfflinePlayer> keys = top_player_participations.keySet();
        List<OfflinePlayer> listKeys = new ArrayList<>( keys );

        return listKeys.indexOf(p) + 1;
    }

    public LinkedHashMap<OfflinePlayer, List<String>> getTopWinsMenuItems() {

        if(menu_wins_data.isEmpty()) {

            // Se estiver vazio, então calcule a lore para cada jogador.
            YamlConfiguration config = MenuConfigFile.get("top_players");
            int position = 1;

            for(OfflinePlayer p: top_player_wins.keySet()) {

                List<String> lore = new ArrayList<>();
                int total_wins = getPlayerWins(p) != null ? getPlayerWins(p).values().stream().reduce(0, Integer::sum) : 0;
                int total_participations = getPlayerParticipations(p) != null ? getPlayerParticipations(p).values().stream().reduce(0, Integer::sum) : 0;

                int player_top_wins_position = getPlayerTopWinsPosition(p);
                int player_top_participations_position = getPlayerTopParticipationsPosition(p);

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

                menu_wins_data.put(p, lore);
                position++;

            }

        }

        return menu_wins_data;

    }

    public LinkedHashMap<OfflinePlayer, List<String>> getTopParticipations() {

        if(menu_participations_data.isEmpty()) {

            // Se estiver vazio, então calcule a lore para cada jogador.
            YamlConfiguration config = MenuConfigFile.get("top_players");
            int position = 1;

            for(OfflinePlayer p: top_player_participations.keySet()) {

                List<String> lore = new ArrayList<>();
                int total_wins = getPlayerWins(p) != null ? getPlayerWins(p).values().stream().reduce(0, Integer::sum) : 0;
                int total_participations = getPlayerParticipations(p) != null ? getPlayerParticipations(p).values().stream().reduce(0, Integer::sum) : 0;

                int player_top_wins_position = getPlayerTopWinsPosition(p);
                int player_top_participations_position = getPlayerTopParticipationsPosition(p);

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

                menu_participations_data.put(p, lore);
                position++;

            }

        }

        return menu_participations_data;

    }

    public LinkedHashMap<OfflinePlayer, Integer> getPlayerTopWinsList() {
        return top_player_wins;
    }

    public LinkedHashMap<OfflinePlayer, Integer> getPlayerTopParticipationsList() {
        return top_player_participations;
    }

    public Map<String, String> getLegendChatTags() {
        return lc_tags;
    }

    public Map<OfflinePlayer, List<String>> getLegendChatTagHolders() {
        return lc_tag_holders;
    }

    public void calculateTopWins() {

        Map<OfflinePlayer, Integer> p_wins = new HashMap<>();

        for(OfflinePlayer p: player_wins.keySet()) {
            int wins = player_wins.get(p).values().stream().reduce(0, Integer::sum);
            p_wins.put(p, wins);
        }

        top_player_wins = p_wins.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry2, LinkedHashMap::new));

    }

    public void calculateTopParticipations() {

        Map<OfflinePlayer, Integer> p_participations = new HashMap<>();

        for(OfflinePlayer p: player_participations.keySet()) {
            int participations = player_participations.get(p).values().stream().reduce(0, Integer::sum);
            p_participations.put(p, participations);
        }

        top_player_participations = p_participations.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry2, LinkedHashMap::new));

    }

}
