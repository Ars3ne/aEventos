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
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.sql.*;

@SuppressWarnings("unchecked")
public class ConversorConnectionManager {

    private Connection connection;
    private final JSONParser parser = new JSONParser();

    private void openConnection() throws SQLException, ClassNotFoundException{

        ConfigurationSection config_section = aEventos.getInstance().getConfig().getConfigurationSection("Conversor.MySQL");

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
            File DatabaseFile = new File(aEventos.getInstance().getDataFolder(), "convert.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DatabaseFile);

        }

    }

    public boolean setup() {

        if(connection == null) { // Se a conexão não foi feita, tente fazer-la.
            try {
                openConnection();
            } catch (ClassNotFoundException | SQLException e) {
                Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível se conectar ao banco de dados do plugin antigo. Desativando plugin...");
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
                aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
                return false;
            }
        }

        return true;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cNão foi possível emcerrar a conexão com o banco de dados do plugin antigo.");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }
    }

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

    public boolean convertHEventos() {

        try {
            PreparedStatement statement = connection
                    .prepareStatement("SELECT player,wins,participations FROM eventos");
            ResultSet results = statement.executeQuery();
            while(results.next()) {

                OfflinePlayer player = Bukkit.getOfflinePlayer(results.getString("player"));

                aEventos.getConnectionManager().insertUser(player.getUniqueId());
                aEventos.getConnectionManager().setTotalWins(player.getUniqueId(), results.getInt("wins"));
                aEventos.getConnectionManager().setTotalParticipations(player.getUniqueId(), results.getInt("participations"));

            }

            return true;

        }catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("§e[aEventos] §cOcorreu um erro ao obter as participações. Desativando plugin...");
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
            aEventos.getPlugin(aEventos.class).getPluginLoader().disablePlugin(aEventos.getPlugin(aEventos.class));
        }

        return false;
    }

}