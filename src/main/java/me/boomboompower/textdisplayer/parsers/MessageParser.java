/*
 *     Copyright (C) 2017 boomboompower
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.boomboompower.textdisplayer.parsers;

import me.boomboompower.textdisplayer.parsers.normal.*;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created to replace the old Placeholder class
 *      due to it's inefficiency
 *
 * @author boomboompower
 */
public abstract class MessageParser {

    /* ParserName | MessageParser */
    private static final HashMap<String, MessageParser> parsers = new HashMap<>();

    /**
     * Default constructor for MessageParser
     *
     * Should be used with MinecraftForge#registerEvents(this);
     */
    public MessageParser() {
    }

    /**
     * Run through all parsers and replace text
     *
     * @param input text to parse
     * @return the formatted text
     */
    public static String parseAll(String input) {
        ParsedMessage message = new ParsedMessage(input);
        for (MessageParser parser : parsers.values()) {
            if (parser.parse(message).getId() != message.getId()) {
                throw new IllegalStateException("ParsedMessage appears to be modifed, use .replace instead!");
            }
        }
        return message.getMessage();
    }

    /**
     * Adds the developers own MessageParser
     *
     * @param parser the developers parser
     */
    public static void addParser(MessageParser parser) {
        if (parser != null && parser.getName() != null) parsers.put(parser.getName(), parser);
    }

    public static void remake() {
        parsers.clear();
        parsers.put("MainParser", new MainParser());
        parsers.put("ItemParser", new ItemParser());
        parsers.put("DirectionParser", new DirectionParser());
        parsers.put("CPSParser", new CPSParser());
        parsers.put("FightingParser", new FightingParser());
        parsers.put("ServerParser", new ServerParser());
    }

    public static MessageParser getParser(String name) {
        return parsers.getOrDefault(name, null);
    }

    public static boolean hasParser(String name) {
        return parsers.containsKey(name);
    }

    /**
     * Returns the name of the specified MessageParser
     *
     * @return the name of the specified MessageParser, cannot be null
     */
    public abstract String getName();

    /**
     * Parses the given text and formats it
     *
     * @param parsedMessage message to format
     * @return the formatted message
     */
    public abstract ParsedMessage parse(final ParsedMessage parsedMessage);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MessageParser && ((MessageParser) obj).getName().equals(getName());
    }

    @Override
    public String toString() {
        return "MessageParser{parsers=" + Arrays.toString(parsers.keySet().toArray()) + '}';
    }
}
