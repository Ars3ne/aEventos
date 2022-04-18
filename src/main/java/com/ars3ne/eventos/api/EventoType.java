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

package com.ars3ne.eventos.api;

public enum EventoType {
    SIGN,
    CAMPO_MINADO,
    SPLEEF,
    SEMAFORO,
    BATATA_QUENTE,
    FROG,
    FIGHT,
    KILLER,
    SUMO,
    FALL,
    PAINTBALL,
    VOTACAO,
    HUNTER,
    QUIZ,
    ANVIL,
    LOTERIA,
    BOLAO,
    GUERRA,
    MATEMATICA,
    PALAVRA,
    FAST_CLICK,
    NEXUS,
    SORTEIO,
    THOR,
    BATTLE_ROYALE,
    NONE;

    public static EventoType getEventoType(String type) {
        switch (type.toLowerCase()) {
            case "sign":
                return EventoType.SIGN;
            case "campominado":
                return EventoType.CAMPO_MINADO;
            case "spleef":
                return EventoType.SPLEEF;
            case "semaforo":
                return EventoType.SEMAFORO;
            case "batataquente":
                return EventoType.BATATA_QUENTE;
            case "frog":
                return EventoType.FROG;
            case "fight":
                return EventoType.FIGHT;
            case "killer":
                return EventoType.KILLER;
            case "sumo":
                return EventoType.SUMO;
            case "fall":
                return EventoType.FALL;
            case "paintball":
                return EventoType.PAINTBALL;
            case "votacao":
                return EventoType.VOTACAO;
            case "hunter":
                return EventoType.HUNTER;
            case "quiz":
                return EventoType.QUIZ;
            case "anvil":
                return EventoType.ANVIL;
            case "loteria":
                return EventoType.LOTERIA;
            case "bolao":
                return EventoType.BOLAO;
            case "guerra":
                return EventoType.GUERRA;
            case "matematica":
                return EventoType.MATEMATICA;
            case "palavra":
                return EventoType.PALAVRA;
            case "fastclick":
                return EventoType.FAST_CLICK;
            case "nexus":
                return EventoType.NEXUS;
            case "sorteio":
                return EventoType.SORTEIO;
            case "thor":
                return EventoType.THOR;
            case "battleroyale":
                return EventoType.BATTLE_ROYALE;
            default:
                return EventoType.NONE;
        }
    }

    public static String getString(EventoType type) {
        switch (type) {
            case SIGN:
                return "sign";
            case CAMPO_MINADO:
                return "campominado";
            case SPLEEF:
                return "spleef";
            case SEMAFORO:
                return "semaforo";
            case BATATA_QUENTE:
                return "batataquente";
            case FROG:
                return "frog";
            case FIGHT:
                return "fight";
            case KILLER:
                return "killer";
            case SUMO:
                return "sumo";
            case FALL:
                return "fall";
            case PAINTBALL:
                return "paintball";
            case VOTACAO:
                return "votacao";
            case HUNTER:
                return "hunter";
            case QUIZ:
                return "quiz";
            case ANVIL:
                return "anvil";
            case LOTERIA:
                return "loteria";
            case BOLAO:
                return "bolao";
            case GUERRA:
                return "guerra";
            case MATEMATICA:
                return "matematica";
            case PALAVRA:
                return "palavra";
            case FAST_CLICK:
                return "fastclick";
            case NEXUS:
                return "nexus";
            case SORTEIO:
                return "sorteio";
            case THOR:
                return "thor";
            case BATTLE_ROYALE:
                return "battleroyale";
            default:
                return "none";
        }
    }

    public static boolean isEventoChat(EventoType type) {
        switch(type) {
            case VOTACAO: case LOTERIA: case BOLAO: case MATEMATICA: case PALAVRA: case FAST_CLICK: case SORTEIO:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEventoGuild(EventoType type) {
        return type == EventoType.GUERRA;
    }

}
