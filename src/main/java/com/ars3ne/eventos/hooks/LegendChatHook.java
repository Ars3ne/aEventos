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

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.ars3ne.eventos.aEventos;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class LegendChatHook implements Listener {

    @EventHandler
    private void onChat(ChatMessageEvent e) {

        OfflinePlayer p = e.getSender();

        // Não estou orgulhoso da gambiarra, mas é o que temos para hoje.
        if(aEventos.getCacheManager().getLegendChatTagHolders().containsKey(p)) {
            // Obtenha todas as tags do usuário
            List<String> tags = aEventos.getCacheManager().getLegendChatTagHolders().get(p);
            for(String tag: tags) {
                if(tag == null || aEventos.getCacheManager().getLegendChatTags().get(tag) == null) continue;
                e.setTagValue(tag, aEventos.getCacheManager().getLegendChatTags().get(tag));
            }
        }
    }

}