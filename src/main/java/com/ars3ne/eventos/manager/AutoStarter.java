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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.Calendar;
public class AutoStarter {

    private BukkitTask task;
    private YamlConfiguration config;
    private boolean start_cooldown = false;

    public void setup() {

        // Se o AutoStart está desativado, retorne.
        if(!aEventos.getInstance().getConfig().getBoolean("AutoStart.Enabled")) return;

        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(aEventos.getInstance(), () -> {

            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int day = cal.get(Calendar.DAY_OF_WEEK);

            // Se estiver um evento acontecendo no momento, retorne.
            if(aEventos.getEventoManager().getEvento() != null || aEventos.getEventoChatManager().getEvento() != null) return;

            // Obtenha todas as strings da configuração e a separe.
            for (String s: aEventos.getInstance().getConfig().getStringList("AutoStart.Times")) {

                String[] separated = s.split("-");
                if(separated.length == 3) {

                    if(day != getDay(separated[0])) continue;

                    if (hour == getHour(separated[2].split(":")[0])
                            && minute == getMinute(separated[2].split(":")[1])
                            && cal.get(Calendar.SECOND) <= 10) {

                        // Se o evento não existe, envie um erro para o console.
                        if(!EventoConfigFile.exists(separated[1])) {
                            Bukkit.getConsoleSender().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Invalid event").replace("&", "§"));
                            continue;
                        }

                        // Inicie o evento.
                        config = EventoConfigFile.get(separated[1]);

                        if(EventoType.isEventoChat(EventoType.getEventoType(config.getString("Evento.Type")))) {

                            if(this.start_cooldown) continue; // Se estiver em cooldown, retorne.
                            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoChatManager().startEvento(EventoType.getEventoType(config.getString("Evento.Type")), config), 20L);

                            this.start_cooldown = true; // Defina o start_cooldown como true.
                            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> this.start_cooldown = false, 220L);

                        }else {
                            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoManager().startEvento(EventoType.getEventoType(config.getString("Evento.Type")), config), 20L);
                        }

                    }

                }else if(separated.length == 2){
                    if (hour == getHour(separated[1].split(":")[0])
                            && minute == getMinute(separated[1].split(":")[1])
                            && cal.get(Calendar.SECOND) <= 10) {

                        // Se o evento não existe, envie um erro para o console.
                        if(!EventoConfigFile.exists(separated[0])) {
                            Bukkit.getConsoleSender().sendMessage(aEventos.getInstance().getConfig().getString("Messages.Invalid event").replace("&", "§"));
                            return;
                        }

                        // Inicie o evento.
                        config = EventoConfigFile.get(separated[0]);

                        if(EventoType.isEventoChat(EventoType.getEventoType(config.getString("Evento.Type")))) {

                            if(this.start_cooldown) return; // Se estiver em cooldown, retorne.
                            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoChatManager().startEvento(EventoType.getEventoType(config.getString("Evento.Type")), config), 20L);

                            this.start_cooldown = true; // Defina o start_cooldown como true.
                            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> this.start_cooldown = false, 220L);

                        }else {
                            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> aEventos.getEventoManager().startEvento(EventoType.getEventoType(config.getString("Evento.Type")), config), 20L);
                        }
                    }
                }
            }
        }, 0L, 100L);

    }

    public void stop() { if(this.task != null) this.task.cancel(); }

    private int getDay(String day) {
        switch (day.toLowerCase()) {
            case "sunday": case "domingo":
                day = "1";
                break;
            case "monday": case "segunda":
                day = "2";
                break;
            case "tuesday": case "terca": case "terça":
                day = "3";
                break;
            case "wednesday": case "quarta":
                day = "4";
                break;
            case "thursday": case "quinta":
                day = "5";
                break;
            case "friday": case "sexta":
                day = "6";
                break;
            case "saturday": case "sábado": case "sabado":
                day = "7";
                break;
            default:
                break;
        }
        return Integer.parseInt(day);
    }

    private int getHour(String hour) {
        switch (hour) {
            case "00": {
                hour = "0";
                break;
            }
            case "01": {
                hour = "1";
                break;
            }
            case "02": {
                hour = "2";
                break;
            }
            case "03": {
                hour = "3";
                break;
            }
            case "04": {
                hour = "4";
                break;
            }
            case "05": {
                hour = "5";
                break;
            }
            case "06": {
                hour = "6";
                break;
            }
            case "07": {
                hour = "7";
                break;
            }
            case "08": {
                hour = "8";
                break;
            }
            case "09": {
                hour = "9";
                break;
            }
            default:
                break;
        }
        return Integer.parseInt(hour);
    }

    private int getMinute(String minute) {
        switch (minute) {
            case "00": {
                minute = "0";
                break;
            }
            case "01": {
                minute = "1";
                break;
            }
            case "02": {
                minute = "2";
                break;
            }
            case "03": {
                minute = "3";
                break;
            }
            case "04": {
                minute = "4";
                break;
            }
            case "05": {
                minute = "5";
                break;
            }
            case "06": {
                minute = "6";
                break;
            }
            case "07": {
                minute = "7";
                break;
            }
            case "08": {
                minute = "8";
                break;
            }
            case "09": {
                minute = "9";
                break;
            }
            default:
                break;
        }
        return Integer.parseInt(minute);
    }
    
}
