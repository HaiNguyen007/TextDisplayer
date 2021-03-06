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

package me.boomboompower.textdisplayer.gui;

import me.boomboompower.textdisplayer.TextDisplayerMod;
import me.boomboompower.textdisplayer.gui.utils.ModernButton;
import me.boomboompower.textdisplayer.utils.ChatColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//        - 94
//        - 70
//        - 46
//        - 22
//        + 2
//        + 26
//        + 50
//        + 74

public class ClearGui extends GuiScreen {

    private GuiScreen previousScreen;

    public ClearGui(GuiScreen previous) {
        this.previousScreen = previous;
    }

    @Override
    public void initGui() {
        this.buttonList.add(new ModernButton(0, this.width / 2 - 160, this.height / 2 + 2, 150, 20, "Cancel"));
        this.buttonList.add(new ModernButton(1, this.width / 2 + 13, this.height / 2 + 2, 150, 20, "Confirm"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawTitle("Message clear confirmation");

        drawCenteredString(this.mc.fontRendererObj, String.format("Are you sure you wish to clear %s %s from the screen?", ChatColor.GOLD.toString() + TextDisplayerMod.getInstance().getLoader().getMessages().size() + ChatColor.WHITE,
                TextDisplayerMod.getInstance().getLoader().getMessages().size() == 1 ? "entry" : "entries"),
                this.width / 2, this.height / 2 - 40, Color.WHITE.getRGB()
        );

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:
                clear();
                break;
            default:
                displayPrevious("Cancelled clearing operation");
                break;
        }
    }

    @Override
    public void sendChatMessage(String msg) {
        TextDisplayerMod.getInstance().sendMessage(msg);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void display() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        MinecraftForge.EVENT_BUS.unregister(this);
        Minecraft.getMinecraft().displayGuiScreen(this);
    }

    private void drawTitle(String text) {
        drawCenteredString(this.mc.fontRendererObj, text, this.width / 2, this.height / 2 - 75, Color.WHITE.getRGB());
        drawHorizontalLine(this.width / 2 - this.mc.fontRendererObj.getStringWidth(text) / 2 - 5, this.width / 2 + this.mc.fontRendererObj.getStringWidth(text) / 2 + 5, this.height / 2 - 65, Color.WHITE.getRGB());
    }

    private void clear() {
        boolean failed = false;

        TextDisplayerMod.getInstance().getLoader().getMessages().clear();
        try {
            FileUtils.deleteDirectory(TextDisplayerMod.getInstance().getLoader().getMainDir());
        } catch (IOException ex) {
            failed = true;
        }

        log(failed ? "Failed to clear all display messages" : "Removed all display messages!");
        displayPrevious(failed ? "Failed to clear messages!" : "&aSuccesfully removed all messages!");
    }

    private void log(String message, Object... replacements) {
        Logger.getLogger("TextDisplayer").log(Level.ALL, String.format(message, replacements));
    }

    private void displayPrevious(String message) {
        if (this.previousScreen instanceof MainGui) {
            this.mc.displayGuiScreen(new MainGui(message));
        } else {
            this.mc.displayGuiScreen(this.previousScreen);
        }
    }
}
