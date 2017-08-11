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

package me.boomboompower.textdisplayer.loading;

import com.google.gson.JsonObject;

import me.boomboompower.textdisplayer.TextDisplayer;
import me.boomboompower.textdisplayer.parsers.MessageParser;
import me.boomboompower.textdisplayer.utils.ChatColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/*
 * Created by boomboompower on 20/06/2017.
 */
public class Message {

    private String fileLocation;

    private String name;
    private String message;

    private boolean isChroma = false;
    private boolean useShadow = false;
    private boolean isDragging = false;

    private float scale;

    private int x;
    private int y;

    public Message(JsonObject object) {
        this.name = object.has("name") ? object.get("name").getAsString() : "unknown";
        this.message = object.has("message") ? object.get("message").getAsString() : "unknown";
        this.isChroma = object.has("usechroma") && object.get("usechroma").getAsBoolean();
        this.useShadow = object.has("useshadow") && object.get("useshadow").getAsBoolean();
        this.scale = object.has("scale") ? object.get("scale").getAsFloat() : 1;
        this.x = object.has("x") ? object.get("x").getAsInt() : 0;
        this.y = object.has("y") ? object.get("y").getAsInt() : 0;

        this.fileLocation = TextDisplayer.getInstance().getLoader().getMainDir().getPath() + "\\" + formatName(this.name).toLowerCase() + ".info";
    }

    /*
     * SAVING
     */

    public boolean configExists() {
        return new File(fileLocation).exists();
    }

    public void save() {
        if (this.name == null || this.message == null) {
            return;
        }
        try {
            if (!TextDisplayer.getInstance().getLoader().getMainDir().exists()) {
                TextDisplayer.getInstance().getLoader().getMainDir().mkdirs();
            }
            JsonObject config = new JsonObject();
            File configFile = new File(fileLocation);

            configFile.createNewFile();
            FileWriter writer = new FileWriter(configFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            config.addProperty("name", this.name);
            config.addProperty("message", this.message);
            config.addProperty("usechroma", this.isChroma);
            config.addProperty("useshadow", this.useShadow);
            config.addProperty("scale", this.scale);
            config.addProperty("x", this.x);
            config.addProperty("y", this.y);

            bufferedWriter.write(config.toString());
            bufferedWriter.close();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void remove() {
        if (this.name == null || this.message == null) {
            return;
        }

        boolean failed = false;

        try {
            FileUtils.forceDelete(new File(fileLocation));
            TextDisplayer.getInstance().getLoader().getMessages().remove(this);
        } catch (Exception ex) {
            failed = true;
        }

        TextDisplayer.getInstance().sendMessage(failed ?
                String.format(ChatColor.RED + "Could not delete %s!", ChatColor.GOLD + formatName(this.name) + ChatColor.RED) :
                String.format(ChatColor.GREEN + "Successfully deleted %s!", ChatColor.GOLD + formatName(this.name) + ChatColor.GREEN)
        );
    }

    /*
     * GETTERS
     */

    public String getName() {
        return this.name;
    }

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes(MessageParser.parseAll(this.message));
    }

    public String getRawMessage() {
        return this.message;
    }

    public boolean useShadow() {
        return this.useShadow;
    }

    public boolean isChroma() {
        return this.isChroma;
    }

    public boolean isDragging() {
        return this.isDragging;
    }

    public float getScale() {
        return this.scale;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getStringWidth() {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(ChatColor.translateAlternateColorCodes(MessageParser.parseAll(this.message)));
    }

    /*
     * SETTERS
     */

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUseShadow(boolean useShadow) {
        this.useShadow = useShadow;
    }

    public void setUseChroma(boolean useChroma) {
        this.isChroma = useChroma;
    }

    public void setDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /*
     * RENDERING
     */

    public void render(boolean drawBox) {
        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        int width = getStringWidth() + 4;
        int height = 14;

        if (getX() < 0) {
             setX(0);
        } else if (getX() > res.getScaledWidth() - width) {
            setX(res.getScaledWidth() - width);
        }

        if (getY() < 0) {
            setY(0);
        } else if (getY() > res.getScaledHeight() - height) {
            setY(res.getScaledHeight() - height);
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(getScale(), getScale(), getScale());
        if (drawBox) {
            Gui.drawRect(getX(), getY(), getX() + width, getY() + height, -1442840576);
        }

        Minecraft.getMinecraft().fontRendererObj.drawString(getMessage(), getX() + 2, getY() + 3, isChroma() ? getColor() : Color.WHITE.getRGB(), useShadow());
        GlStateManager.popMatrix();
    }

    /*
     * MISC
     */

    public String formatName(String name) {
        char[] charList = name.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : charList) {
            if (Character.isLetterOrDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString().trim().length() > 0 ? builder.toString().trim().length() > 50 ? builder.toString().trim().substring(0, 10) : builder.toString().trim() : "x";
    }

    public static int getColor() {
        return Color.HSBtoRGB(System.currentTimeMillis() % 2500L / 2500.0f, 0.8f, 0.8f);
    }
}
