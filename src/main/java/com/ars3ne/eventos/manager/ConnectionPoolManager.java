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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private HikariDataSource dataSource;

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    private int minimumConnections;
    private int maximumConnections;
    private long connectionTimeout;

    public ConnectionPoolManager() {
        init();
        setupPool();
    }

    private void init() {

        hostname = aEventos.getInstance().getConfig().getString("MySQL.Host");
        port = aEventos.getInstance().getConfig().getInt("MySQL.Port");
        database = aEventos.getInstance().getConfig().getString("MySQL.Database");
        username = aEventos.getInstance().getConfig().getString("MySQL.Username");
        password = aEventos.getInstance().getConfig().getString("MySQL.Password");
        minimumConnections = 3;
        maximumConnections = 30;
        connectionTimeout = 10000;
    }

    private void setupPool() {

        HikariConfig config = new HikariConfig();

        if(aEventos.getInstance().getConfig().getBoolean("MySQL.Enabled")) {

            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);

            config.setUsername(username);
            config.setPassword(password);
            config.setMinimumIdle(minimumConnections);
            config.setMaximumPoolSize(maximumConnections);
            config.setConnectionTimeout(connectionTimeout);
            config.setConnectionTestQuery("SELECT 1");

            dataSource = new HikariDataSource(config);

        }
    }

    public java.sql.Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close(boolean is_sqlite, java.sql.Connection conn, PreparedStatement ps, ResultSet res) {
        if (!is_sqlite && conn != null) try { conn.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        if (res != null) try { res.close(); } catch (SQLException ignored) {}
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
