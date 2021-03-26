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
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {

    private static final String CURERNT_VERSION = aEventos.getInstance().getDescription().getVersion();

    public static void verify() {


        Bukkit.getScheduler().runTaskAsynchronously(aEventos.getInstance(), () -> {

            try {

                final JSONParser parser = new JSONParser();

                URL url = new URL("https://api.github.com/repos/ars3ne/aEventos/releases");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "application/json");

                StringBuilder content;

                try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    content = new StringBuilder();
                    while ((line = input.readLine()) != null) {
                        content.append(line);
                        content.append(System.lineSeparator());
                    }
                } finally {
                    connection.disconnect();
                }

                JSONArray json = (JSONArray) parser.parse(content.toString());

                for(Object o: json){
                    if ( o instanceof JSONObject ) {

                        JSONObject release = (JSONObject) o;

                        if(release.get("tag_name").equals(CURERNT_VERSION)) break;
                        if((Boolean) release.get("draft") || (Boolean) release.get("prerelease")) continue;

                        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aUma nova atualização está disponível! (" + release.get("name") + ")");
                        Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aVocê pode baixar-la aqui: " + release.get("html_url"));
                        return;
                    }
                }

                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §aVocê está usando a última versão. (v" + CURERNT_VERSION + ")");


            } catch (IOException | ParseException e) {
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível verificar por atualizações.");
                e.printStackTrace();
            }


        });

    }
}
