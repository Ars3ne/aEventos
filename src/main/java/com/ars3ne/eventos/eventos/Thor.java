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
import com.ars3ne.eventos.listeners.eventos.ThorListener;
import com.iridium.iridiumcolorapi.IridiumColorAPI;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Thor extends Evento {

    private final YamlConfiguration config;
    private final ThorListener listener = new ThorListener();

    private final int time;
    private final int delay;

    private BukkitTask runnable;
    private final BukkitScheduler scheduler = aEventos.getInstance().getServer().getScheduler();

    private final Random random  = new Random();

    public Thor(YamlConfiguration config) {
        super(config);
        this.config = config;
        this.time = config.getInt("Evento.Time");
        this.delay = config.getInt("Evento.Delay");
    }

    @Override
    public void start() {

        // Registre o listener do evento
        aEventos.getInstance().getServer().getPluginManager().registerEvents(listener, aEventos.getInstance());
        listener.setEvento();

        // Envie a mensagem para todos os usuários no evento.
        List<String> starting_level = config.getStringList("Messages.Starting");
        for (Player player : getPlayers()) {
            for(String s : starting_level) {
                player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@time", String.valueOf(time)).replace("@name", config.getString("Evento.Title"))));
            }
        }

        for (Player player : getSpectators()) {
            for(String s : starting_level) {
                player.sendMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@time", String.valueOf(time)).replace("@name", config.getString("Evento.Title"))));
            }
        }


        // Depois do tempo da config, inicie o evento.
        runnable = scheduler.runTaskTimer(aEventos.getInstance(), () -> {

            if(!isHappening()) runnable.cancel();

            if(getPlayers().size() > 1) {

                // Invoque um raio em cima de um jogador aleatório.
                Player p = getPlayers().get(random.nextInt(getPlayers().size()));
                p.getWorld().spawnEntity(p.getLocation(), EntityType.LIGHTNING);

            }else {
                win();
                runnable.cancel();
            }

        }, time * 20L, delay * 20L);
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
                executeConsoleCommand(p, s.replace("@winner", p.getName()));
            }

            // Adicione o nome á lista de vencedores.
            winners.add(p.getName());
        }

        // Mande a mensagem de vitória para o servidor.
        List<String> broadcast_messages = this.config.getStringList("Messages.Winner");
        for(String s : broadcast_messages) {
            aEventos.getInstance().getServer().broadcastMessage(IridiumColorAPI.process(s.replace("&", "§").replace("@winner", String.join(", ", winners)).replace("@name", config.getString("Evento.Title"))));
        }

    }

    @Override
    public void stop() {
        // Remova o listener do evento e chame a função cancel.
        HandlerList.unregisterAll(listener);
        this.removePlayers();
    }

}
