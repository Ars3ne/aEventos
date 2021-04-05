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
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class ConnectionManager {

    private Connection connection;
    private final JSONParser parser = new JSONParser();

    private void openConnection() throws SQLException, ClassNotFoundException{

        ConfigurationSection config_section = aEventos.getInstance().getConfig().getConfigurationSection("MySQL");

        boolean connectionType = config_section.getBoolean("Enabled");

        if (connectionType){

            String host = config_section.getString("Host");
            int port = config_section.getInt("Port");
            String username = config_section.getString("Username");
            String password = config_section.getString("Password");
            String database = config_section.getString("Database");

            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

        }else{

            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("org.sqlite.JDBC");
            File DatabaseFile = new File(aEventos.getInstance().getDataFolder(), "storage.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DatabaseFile);

        }

    }

    public boolean setup() {

        if(connection == null) { // Se a conexão não foi feita, tente fazer-la.
            try {
                openConnection();
            } catch (ClassNotFoundException | SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível se conectar ao banco de dados. Desativando plugin...");
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
                aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
                return false;
            }
        }

        try {

            PreparedStatement statement, statement2, statement3;
            if(aEventos.getInstance().getConfig().getBoolean("MySQL.Enabled")) {
               statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `aeventos_users` ( `id` integer PRIMARY KEY AUTO_INCREMENT NOT NULL, `username` TEXT NOT NULL , `uuid` TEXT NOT NULL , `total_wins` INT NOT NULL , `total_participations` INT NOT NULL , `wins` TEXT NOT NULL , `participations` TEXT NOT NULL )");
               statement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `aeventos_eventos` ( `id` integer PRIMARY KEY AUTO_INCREMENT NOT NULL, `name` TEXT NOT NULL , `current_winners` TEXT NOT NULL )");
               statement3 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `aeventos_eventos_guild` ( `id` integer PRIMARY KEY AUTO_INCREMENT NOT NULL, `name` TEXT NOT NULL , `current_guild_winner` TEXT NOT NULL, `total_kills` TEXT NOT NULL, `current_winners` TEXT NOT NULL )");
            }else {
                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `aeventos_users` ( `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL, `username` TEXT NOT NULL , `uuid` TEXT NOT NULL , `total_wins` INT NOT NULL , `total_participations` INT NOT NULL , `wins` TEXT NOT NULL , `participations` TEXT NOT NULL )");
                statement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `aeventos_eventos` ( `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL , `current_winners` TEXT NOT NULL )");
                statement3 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `aeventos_eventos_guild` ( `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL , `current_guild_winner` TEXT NOT NULL, `total_kills` TEXT NOT NULL, `current_winners` TEXT NOT NULL )");
            }

            statement.executeUpdate();
            statement2.executeUpdate();
            statement3.executeUpdate();

        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível criar o banco de dados. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
            return false;
        }

        return true;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível emcerrar a conexão com o banco de dados.");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }
    }

    /*public Connection getConnection() {
        return connection;
    }*/

    public void createEvento(String name) {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT name FROM aeventos_eventos WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();

            if(!results.next()) {
                PreparedStatement insert = connection
                        .prepareStatement("INSERT INTO aeventos_eventos (name, current_winners) VALUES (?,?)");
                insert.setString(1, name);
                insert.setString(2, "[]");
                insert.executeUpdate();
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao inserir um evento na database. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

    }

    public void createEventoGuild(String name) {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT name FROM aeventos_eventos_guild WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();

            if(!results.next()) {
                PreparedStatement insert = connection
                        .prepareStatement("INSERT INTO aeventos_eventos_guild (name, current_guild_winner, total_kills, current_winners) VALUES (?,?,?,?)");
                insert.setString(1, name);
                insert.setString(2, "[]");
                insert.setString(3, "[]");
                insert.setString(4, "[]");
                insert.executeUpdate();
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao inserir um evento na database. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

    }

    public void insertUser(UUID uuid) {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT username FROM aeventos_users WHERE uuid=?");
            statement.setString(1,uuid.toString());

            ResultSet results = statement.executeQuery();
            if(!results.next()) {

                PreparedStatement insert = connection
                        .prepareStatement("INSERT INTO aeventos_users (username, uuid, total_wins, total_participations, wins, participations) VALUES (?,?,?,?,?,?)");
                insert.setString(1, Bukkit.getOfflinePlayer(uuid).getName());
                insert.setString(2, uuid.toString());
                insert.setInt(3, 0);
                insert.setInt(4, 0);
                insert.setString(5, "{}");
                insert.setString(6, "{}");
                insert.executeUpdate();

            }
        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao inserir um usuário. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

    }


    public String getWins(UUID uuid) {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT wins FROM aeventos_users WHERE uuid=?");
            statement.setString(1,uuid.toString());
            ResultSet results = statement.executeQuery();
            if(results.next()) {
                return results.getString("wins");
            }else {
                return "{}";
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao obter as vitorias de um usuário. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

        return null;

    }

    public void addWin(String name, UUID uuid) {

        try {
            JSONObject json = (JSONObject) parser.parse(getWins(uuid));

            if(!json.containsKey(name)) json.put(name, 0);

            int wins = Integer.parseInt(json.get(name).toString());
            json.remove(name);
            json.put(name, wins + 1);

            PreparedStatement update = connection
                    .prepareStatement("UPDATE aeventos_users SET wins=?,total_wins=total_wins+1 WHERE uuid=?");
            update.setObject(1, json.toString());
            update.setString(2, uuid.toString());
            update.executeUpdate();

        } catch (ParseException | SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao adicionar uma vitória. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }
    }

    public String getParticipations(UUID uuid) {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT participations FROM aeventos_users WHERE uuid=?");
            statement.setString(1,uuid.toString());
            ResultSet results = statement.executeQuery();
            if(results.next()) {
                return results.getString("participations");
            }else {
                return "{}";
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao obter as participações de um usuário. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

        return null;

    }

    public void addParticipation(String name, UUID uuid) {

        try {
            JSONObject json = (JSONObject) parser.parse(getParticipations(uuid));

            if(!json.containsKey(name)) json.put(name, 0);

            int wins = Integer.parseInt(json.get(name).toString());
            json.remove(name);
            json.put(name, wins + 1);

            PreparedStatement update = connection
                    .prepareStatement("UPDATE aeventos_users SET participations=?,total_participations=total_participations+1 WHERE uuid=?");
            update.setObject(1, json.toString());
            update.setString(2, uuid.toString());
            update.executeUpdate();

        } catch (ParseException | SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao adicionar uma participação. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }
    }

    public String getEventoWinners(String name) {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT current_winners FROM aeventos_eventos WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();
            if(results.next()) {
                return results.getString("current_winners");
            }else {
                return "[]";
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao obter os vencedores de um evento. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

        return null;
    }

    public String getEventoGuildWinners(String name) {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT current_winners FROM aeventos_eventos_guild WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();
            if(results.next()) {
                return results.getString("current_winners");
            }else {
                return "[]";
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao obter os vencedores de um evento. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

        return null;
    }

    public String getEventoGuildKills(String name) {

        Gson gson = new Gson();

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT total_kills FROM aeventos_eventos_guild WHERE name=?");
            statement.setString(1,name);
            ResultSet results = statement.executeQuery();
            if(results.next()) {
                return gson.toJson(results.getString("total_kills"));
            }else {
                return "[]";
            }

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao obter as kills de um evento. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

        return null;
    }

    public void setEventoWinner(String name, List<String> winner) {

        try {
            PreparedStatement update = connection
                    .prepareStatement("UPDATE aeventos_eventos SET current_winners=? WHERE name=?");
            update.setString(1, String.valueOf(winner));
            update.setString(2, name);
            update.executeUpdate();

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao definir o vencedor do evento. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

    }

    public void setEventoGuildWinner(String name, String guild_name, HashMap<OfflinePlayer, Integer> total_kills, List<String> winner) {

        Gson gson = new Gson();

        try {
            PreparedStatement update = connection
                    .prepareStatement("UPDATE aeventos_eventos_guild SET current_guild_winner=?,total_kills=?,current_winners=? WHERE name=?");
            update.setString(1, guild_name);
            update.setString(2, gson.toJson(total_kills));
            update.setString(3, String.valueOf(winner));
            update.setString(4, name);
            update.executeUpdate();

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao definir o vencedor do evento. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

    }
}