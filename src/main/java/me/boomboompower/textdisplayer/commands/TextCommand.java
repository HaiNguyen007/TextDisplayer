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

package me.boomboompower.textdisplayer.commands;

import me.boomboompower.textdisplayer.TextDisplayer;
import me.boomboompower.textdisplayer.gui.MainGui;
import me.boomboompower.textdisplayer.utils.ChatColor;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.List;

public class TextCommand implements ICommand {

    @Override
    public String getCommandName() {
        return "textdisplayer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return ChatColor.RED + "Usage: /" + getCommandName();
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("text", "textwriter");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (TextDisplayer.getInstance().getWebsiteUtils().isDisabled()) {
            TextDisplayer.getInstance().sendMessage("&cTextDisplayer is currently disabled.");
            TextDisplayer.getInstance().sendMessage("&cCheck back soon for more information!");
        } else {
            if (args.length == 0) {
                new MainGui().display();
            } else {
                new MainGui(true, get(args)).display();
            }
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

    private String get(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (String s : args) {
            builder.append(s);
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
