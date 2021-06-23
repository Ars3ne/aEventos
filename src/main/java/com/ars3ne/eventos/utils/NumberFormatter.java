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

package com.ars3ne.eventos.utils;

import com.ars3ne.eventos.aEventos;

import java.text.DecimalFormat;
import java.util.List;

public class NumberFormatter {

    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final List<String> CHARS = aEventos.getInstance().getConfig().getStringList("Formatter.Letters");

    public static String decimalFormat(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String letterFormat(double value) {

        int index = 0;

        double tmp;
        while ((tmp = value / 1000) >= 1) {
            value = tmp;
            ++index;
        }

        return DECIMAL_FORMAT.format(value) + CHARS.get(index);

    }

    public static String parse(double value) {

        if(aEventos.getInstance().getConfig().getString("Formatter.Type").equalsIgnoreCase("Decimal")) {
            return decimalFormat(value);
        }

        return letterFormat(value);

    }

}
