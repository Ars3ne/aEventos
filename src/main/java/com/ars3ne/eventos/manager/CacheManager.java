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
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class CacheManager {

    private Map<OfflinePlayer, Map<String, Integer>> player_wins = new HashMap<>();
    private Map<OfflinePlayer, Map<String, Integer>> player_participations = new HashMap<>();

    private LinkedHashMap<OfflinePlayer, Integer> top_player_wins = new LinkedHashMap<>();
    private LinkedHashMap<OfflinePlayer, Integer> top_player_participations = new LinkedHashMap<>();

    private Map<String, String> lc_tags = aEventos.getTagManager().getTags();
    private Map<OfflinePlayer, List<String>> lc_tag_holders = aEventos.getTagManager().getTagHolders();

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

        aEventos.getConnectionManager().getPlayersWins();
        aEventos.getConnectionManager().getPlayersParticipations();

        Map<OfflinePlayer, Integer> p_wins = new HashMap<>();
        Map<OfflinePlayer, Integer> p_participations = new HashMap<>();

        for(OfflinePlayer p: player_wins.keySet()) {
            int wins = player_wins.get(p).values().stream().reduce(0, Integer::sum);
            p_wins.put(p, wins);
        }

        for(OfflinePlayer p: player_participations.keySet()) {
            int participations = player_participations.get(p).values().stream().reduce(0, Integer::sum);
            p_participations.put(p, participations);
        }

        top_player_wins = p_wins.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry2, LinkedHashMap::new));
        top_player_participations = p_participations.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry2, LinkedHashMap::new));

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
}
