/*
 *     Copyright (C) 2016 boomboompower
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

import me.boomboompower.textdisplayer.TextDisplayer;

import me.boomboompower.textdisplayer.loading.Message;
import me.boomboompower.textdisplayer.utils.ChatColor;
import me.boomboompower.textdisplayer.utils.GlobalUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.apache.commons.io.FileUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

//        - 94
//        - 70
//        - 46
//        - 22
//        + 2
//        + 26
//        + 50
//        + 74

public class SettingsGui extends GuiScreen {

    private static final String ENABLED = ChatColor.GREEN + "Enabled";
    private static final String DISABLED = ChatColor.RED + "Disabled";

    private GuiTextField text;

    private GuiButton add;
    private GuiButton clear;

    private String input = "";
    private String lastClickedName = "";

    private int lastMouseX = 0;
    private int lastMouseY = 0;

    public SettingsGui() {
        this("");
    }

    public SettingsGui(String input) {
        this.input = input;
        this.lastClickedName = "";
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        text = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 75, this.height - 25, 150, 20);

        this.buttonList.add(this.add = new GuiButton(1, 20, this.height - 25, 50, 20, "Add"));
        this.buttonList.add(this.clear = new GuiButton(2, 75, this.height - 25, 50, 20, "Clear"));

        this.buttonList.add(new GuiButton(3, this.width - 120, this.height - 25, 100, 20, "Shadow: " + (TextDisplayer.useShadow ? ENABLED : DISABLED)));

        text.setText(input);
        text.setMaxStringLength(TextDisplayer.MAX_CHARS);

        this.lastClickedName = "";
    }

    @Override
    public void drawScreen(int x, int y, float ticks) {
        drawDefaultBackground();

        drawTitle("TextDisplayer v" + TextDisplayer.VERSION);

        add.enabled = ChatColor.formatUnformat('&', this.text.getText()).length() > 0;
        clear.enabled = TextDisplayer.loader.getMessages().size() > 0;

        text.drawTextBox();
        TextDisplayer.events.renderDisplay(true);
        super.drawScreen(x, y, ticks);
    }

    @Override
    protected void keyTyped(char c, int key)  {
        if (key == 1) {
            mc.displayGuiScreen(null);
        } else {
            text.textboxKeyTyped(c, key);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        try {
            super.mouseClicked(mouseX, mouseY, button);
            text.mouseClicked(mouseX, mouseY, button);
        } catch (Exception ex) {}

        if (button == 0) {
            for (Message m : TextDisplayer.loader.getMessages()) {
                if (this.lastClickedName.equals(m.getName())) {
                    new TextSettingsGui(this, m).display();
                    return;
                }
                int startX = m.getX();
                int startY = m.getY();
                int endX = startX + mc.fontRendererObj.getStringWidth(m.getMessage()) + 4;
                int endY = startY + 14;
                if(mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY) {
                    m.dragging = true;
                    this.lastMouseX = mouseX;
                    this.lastMouseY = mouseY;
                    this.lastClickedName = m.getName();
                }
            }
        } else {
            this.lastClickedName = "";
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int action) {
        super.mouseReleased(mouseX, mouseY, action);
        for (Message m : TextDisplayer.loader.getMessages()) {
            if (m.dragging) m.dragging = false;
        }
    }

    protected void mouseClickMove(int mouseX, int mouseY, int lastButtonClicked, long timeSinceMouseClick) {
        super.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        for (Message m : TextDisplayer.loader.getMessages()) {
            if (m.dragging) {
                m.setX(m.getX() + mouseX - this.lastMouseX);
                m.setY(m.getY() + mouseY - this.lastMouseY);
                this.lastMouseX = mouseX;
                this.lastMouseY = mouseY;
                lastClickedName = "";
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        text.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                String message = ChatColor.formatUnformat('&', this.text.getText());
                if (!message.isEmpty()) {
                    TextDisplayer.loader.create(message.contains(" ") ? message.split(" ")[0] : message, text.getText(), TextDisplayer.useShadow);
                } else {
                    sendChatMessage("No text provided!");
                }
                break;
            case 2:
                new ClearConfirmationGui(this).display();
                break;
            case 3:
                TextDisplayer.useShadow = !TextDisplayer.useShadow;
                button.displayString = "Shadow: " + (TextDisplayer.useShadow ? ENABLED : DISABLED);
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        TextDisplayer.loader.saveAll();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void sendChatMessage(String message) {
        GlobalUtils.sendMessage(message);
    }

    public void display() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        MinecraftForge.EVENT_BUS.unregister(this);
        Minecraft.getMinecraft().displayGuiScreen(this);
    }

    private void drawTitle(String text) {
        drawCenteredString(mc.fontRendererObj, text, this.width / 2, 15, Color.WHITE.getRGB());
        drawHorizontalLine(this.width / 2 - mc.fontRendererObj.getStringWidth(text) / 2 - 5, this.width / 2 + mc.fontRendererObj.getStringWidth(text) / 2 + 5, 25, Color.WHITE.getRGB());
    }
}
