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

package com.ars3ne.eventos.hooks;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.utils.EventoConfigFile;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final aEventos plugin;

    public PlaceholderAPIHook(aEventos plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier(){
        return "aeventos";
    }

    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier){

        if(player == null){
            return "";
        }

        // %aeventos_wins_total%
        if(identifier.equals("wins_total")){
            int value = aEventos.getCacheManager().getPlayerWins(player) != null ? aEventos.getCacheManager().getPlayerWins(player).values().stream().reduce(0, Integer::sum) : 0;
            return String.valueOf(value);
        }

        // %aeventos_participations_total%
        if(identifier.equals("participations_total")){
            int value = aEventos.getCacheManager().getPlayerParticipations(player) != null ? aEventos.getCacheManager().getPlayerParticipations(player).values().stream().reduce(0, Integer::sum) : 0;
            return String.valueOf(value);
        }

        // %aeventos_wins_[NOME DO ARQUIVO DE CONFIGURAÇÃO]%
        if(identifier.contains("wins_")) {

            for(File config: Objects.requireNonNull(EventoConfigFile.getAllFiles())) {

                if(identifier.equals("wins_" + config.getName().substring(0, config.getName().length() - 4))) {
                    if(!aEventos.getCacheManager().getPlayerWins(player).containsKey(config.getName().substring(0, config.getName().length() - 4))) return String.valueOf(0);
                    int value = aEventos.getCacheManager().getPlayerWins(player).get(config.getName().substring(0, config.getName().length() - 4));
                    return String.valueOf(value);
                }

            }

            return "0";

        }

        // %aeventos_participations_[NOME DO ARQUIVO DE CONFIGURAÇÃO]%
        if(identifier.contains("participations_")) {

            for(File config: Objects.requireNonNull(EventoConfigFile.getAllFiles())) {

                if(identifier.equals("participations_" + config.getName().substring(0, config.getName().length() - 4))) {
                    if(!aEventos.getCacheManager().getPlayerParticipations(player).containsKey(config.getName().substring(0, config.getName().length() - 4))) return String.valueOf(0);
                    int value = aEventos.getCacheManager().getPlayerParticipations(player).get(config.getName().substring(0, config.getName().length() - 4));
                    return String.valueOf(value);
                }

            }

            return "0";

        }

        // %aeventos_tag_[NOME DO ARQUIVO DE CONFIGURAÇÃO]%
        if(identifier.contains("tag_")) {

            for(String tag_it: aEventos.getCacheManager().getLegendChatTags().keySet()) {

                if(identifier.equals("tag_" + tag_it)) {

                    if(aEventos.getCacheManager().getLegendChatTagHolders().containsKey(player)) {

                        List<String> tags = aEventos.getCacheManager().getLegendChatTagHolders().get(player);

                        for(String tag: tags) {
                            if(tag.equals(tag_it)) return aEventos.getCacheManager().getLegendChatTags().get(tag);
                        }

                    }

                }

            }

            return "";
        }


        return null;
    }
}