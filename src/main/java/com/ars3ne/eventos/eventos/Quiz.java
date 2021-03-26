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

package com.ars3ne.eventos.eventos;

import com.ars3ne.eventos.aEventos;
import com.ars3ne.eventos.api.Evento;
import com.ars3ne.eventos.api.events.PlayerLoseEvent;
import com.ars3ne.eventos.listeners.eventos.QuizListener;
import com.ars3ne.eventos.utils.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Quiz extends Evento {

    private final YamlConfiguration config;
    private final QuizListener listener = new QuizListener();

    private final HashMap<String, String> questions = new HashMap<>();
    private final int time, delay, max_questions;

    private final Cuboid true_cuboid, false_cuboid;

    private BukkitTask runnable;
    private final BukkitScheduler scheduler = aEventos.getInstance().getServer().getScheduler();

    private boolean question_happening;
    private int total_questions;

    public Quiz(YamlConfiguration config) {

        super(config);
        this.config = config;

        time = this.config.getInt("Evento.Time");
        delay = this.config.getInt("Evento.Interval");
        max_questions = this.config.getInt("Evento.Max questions");

        // Obtenha os cuboids.
        World world = aEventos.getInstance().getServer().getWorld(this.config.getString("Locations.Pos1.world"));
        Location pos1 = new Location(world, this.config.getDouble("Locations.Pos1.x"), this.config.getDouble("Locations.Pos1.y"), this.config.getDouble("Locations.Pos1.z"));
        Location pos2 = new Location(world, this.config.getDouble("Locations.Pos2.x"), this.config.getDouble("Locations.Pos2.y"), this.config.getDouble("Locations.Pos2.z"));
        true_cuboid = new Cuboid(pos1, pos2);

        Location pos3 = new Location(world, this.config.getDouble("Locations.Pos3.x"), this.config.getDouble("Locations.Pos3.y"), this.config.getDouble("Locations.Pos3.z"));
        Location pos4 = new Location(world, this.config.getDouble("Locations.Pos4.x"), this.config.getDouble("Locations.Pos4.y"), this.config.getDouble("Locations.Pos4.z"));
        false_cuboid = new Cuboid(pos3, pos4);

        // Obtenha as questões.
        for(String s: config.getStringList("Questions")) {
            String[] separated = s.split("-");
            if(separated.length == 1) {
                questions.put(separated[0], null);
            }else {
                questions.put(separated[0], separated[1]);
            }
        }

    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Inicie a primeira questão
        runnable = scheduler.runTaskTimer(aEventos.getInstance(), () -> {

            if(!isHappening()) runnable.cancel();
            if(!question_happening) {

                if(total_questions < max_questions) {
                    question();
                }else {
                    win();
                    runnable.cancel();
                }

            }
        }, 0, 20L);

    }

    @Override
    public void winner(Player p) {

        // Mande a mensagem de vitória.
        List<String> broadcast_messages = config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", p.getName()).replace("@name", getConfig().getString("Evento.Title")));
        }

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinner(p);

        // Encerre o evento.
        this.stop();

        // Execute todos os comandos de vitória.
        List<String> commands = config.getStringList("Rewards.Commands");
        for(String s : commands) {
            aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), s.replace("@winner", p.getName()));
        }

    }

    public void win() {

        List<String> winners = new ArrayList<>();

        // Adicionar vitória e dar a tag no LegendChat.
        this.setWinners();

        // Encerre o evento.
        this.stop();

        // Obtenha todos os jogadores restantes e os entregue as recompensas.
        for(Player p: getPlayers()) {

            // Execute os comandos de vitória
            List<String> commands = this.config.getStringList("Rewards.Commands");
            for(String s : commands) {
                aEventos.getInstance().getServer().dispatchCommand(aEventos.getInstance().getServer().getConsoleSender(), s.replace("@winner", p.getName()));
            }

            // Adicione o nome á lista de vencedores.
            winners.add(p.getName());
        }

        // Mande a mensagem de vitória para o servidor.
        List<String> broadcast_messages = this.config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(s.replace("&", "§").replace("@winner", String.join(", ", winners)).replace("@name", config.getString("Evento.Title")));
        }

    }

    @Override
    public void stop() {
        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

    private void question() {

        List<String> question_values = new ArrayList<>(questions.keySet());
        List<String> question_broadcast = config.getStringList("Messages.Question");
        List<String> next_question = config.getStringList("Messages.Next question");

        if(isHappening()) {

            total_questions++;
            question_happening = true;

            // Obtenha uma questão aleatória.
            int current_question = new Random().nextInt(question_values.size());

            // Mande a pergunta para todos os participantes.
            for (Player player : getPlayers()) {
                for(String s : question_broadcast) {
                    player.sendMessage(s.replace("&", "§").replace("@currentquestion", String.valueOf(total_questions)).replace("@question", question_values.get(current_question)).replace("@name", this.config.getString("Evento.Title")));
                }
            }

            for (Player player : getSpectators()) {
                for(String s : question_broadcast) {
                    player.sendMessage(s.replace("&", "§").replace("@currentquestion", String.valueOf(total_questions)).replace("@question", question_values.get(current_question)).replace("@name", this.config.getString("Evento.Title")));
                }
            }

            // Depois do tempo da config, verfique a resposta.

            aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> {

                if(isHappening() && question_happening) {

                    List<Player> eliminate = new ArrayList<>();
                    // Elimine todos os jogadores que não estão dentro dos cuboids.
                    for (Player p : getPlayers()) {
                        if (!true_cuboid.isInWithMargeY(p, 6) && !false_cuboid.isInWithMargeY(p, 6)) {
                            // Remova o jogador do evento e envie a mensagem de eliminação.
                            p.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
                            eliminate.add(p);
                            PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
                            Bukkit.getPluginManager().callEvent(lose);
                        }
                    }

                    // Obtenha a resposta e elimine os jogadores que repsonderam incorretamente.
                    String answer = questions.get(question_values.get(current_question));

                    if(answer != null) {

                        if(answer.equalsIgnoreCase("true")) {

                            // Elimine todos os jogadores no cuboid false.
                            for(Player p: getPlayers()) {
                                if (false_cuboid.isInWithMargeY(p, 6)) {
                                    // Remova o jogador do evento e envie a mensagem de eliminação.
                                    p.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
                                    eliminate.add(p);
                                    PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
                                    Bukkit.getPluginManager().callEvent(lose);
                                }
                            }

                        }else {

                            // Elimine todos os jogadores no cuboid true.
                            for(Player p: getPlayers()) {
                                if (true_cuboid.isInWithMargeY(p, 6)) {
                                    // Remova o jogador do evento e envie a mensagem de eliminação.
                                    p.sendMessage(aEventos.getInstance().getConfig().getString("Messages.Eliminated").replace("&", "§"));
                                    eliminate.add(p);
                                    PlayerLoseEvent lose = new PlayerLoseEvent(p, config.getString("filename").substring(0, config.getString("filename").length() - 4), getType());
                                    Bukkit.getPluginManager().callEvent(lose);
                                }
                            }

                        }
                    }

                    // Elimine os jogadores.
                    for(Player p: eliminate) {
                        remove(p);
                    }
                    eliminate.clear();

                    if(!isHappening()) return;

                    // Mande a mensagem de início da próxima pergunta para todos os participantes.
                    for (Player player : getPlayers()) {
                        for(String s : next_question) {
                            player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(delay)).replace("@name", config.getString("Evento.Title")));
                        }
                    }

                    for (Player player : getSpectators()) {
                        for(String s : next_question) {
                            player.sendMessage(s.replace("&", "§").replace("@time", String.valueOf(delay)).replace("@name", config.getString("Evento.Title")));
                        }
                    }

                    aEventos.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(aEventos.getInstance(), () -> question_happening = false, delay * 20L);

                }

            }, time * 20L);

        }
    }

}
