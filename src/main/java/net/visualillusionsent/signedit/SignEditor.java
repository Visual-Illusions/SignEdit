/*
 * This file is part of SignEdit.
 *
 * Copyright © 2013-2014 Visual Illusions Entertainment
 *
 * SignEdit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.signedit;

import net.canarymod.api.entity.living.humanoid.Player;
import net.visualillusionsent.minecraft.plugin.ChatFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author Jason (darkdiplomat)
 */
final class SignEditor {
    private static final File signsDir = new File("config/SignEdit/signs/");

    private final Player player;
    private boolean editing, persistent, copying, pasting;
    private String[] copied;

    static {
        if (!signsDir.exists()) {
            signsDir.mkdirs();
        }
    }

    SignEditor(Player player) {
        this.player = player;
    }

    final boolean isEditing() {
        return editing;
    }

    final boolean isPersistent() {
        return persistent;
    }

    final boolean isCopying() {
        return copying;
    }

    final boolean isPasting() {
        return pasting;
    }

    final SignEditor enableEditing() {
        allOff();
        this.editing = true;
        return this;
    }

    final SignEditor enablePersistance() {
        this.persistent = true;
        return this;
    }

    final void enableCopying() {
        this.editing = true;
        this.copying = true;
        this.pasting = false; //Can't do both, so turn off pasting
        this.persistent = false; // Shouldn't persistently copy text
    }

    final SignEditor enablePasting() {
        this.editing = true;
        this.pasting = true;
        this.copying = false; //Can't do both, so turn off copying
        return this;
    }

    final SignEditor allOff() {
        this.editing = false;
        this.persistent = false;
        this.pasting = false;
        this.copying = false;
        return this;
    }

    final String[] getCopied() {
        return copied.clone();
    }

    final void storeCopied(String[] text) {
        this.copied = text.clone();
    }

    final void loadSignText(String file) {
        Scanner scan = null;
        try {
            scan = new Scanner(new File(signsDir, file.concat(".sign")));
            if (copied == null) {
                copied = new String[4];
            }
            for (int index = 0; index < 4; index++) {
                if (scan.hasNextLine()) {
                    String temp = scan.nextLine();
                    while (temp.startsWith("#") && scan.hasNextLine()) { // remove comments
                        temp = scan.nextLine();
                    }
                    copied[index] = temp.length() > 15 ? temp.substring(0, 15) : temp;
                }
            }
            player.message(ChatFormat.CYAN.concat("Sign text loaded"));
        }
        catch (IOException ioex) {
            player.notice("Failed to load text: " + ioex.getMessage());
        }
        finally {
            if (scan != null) {
                scan.close();
            }
        }
    }

    final boolean saveSignText(String fileName) {
        boolean ret = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File(signsDir, fileName.concat(".sign")));
            for (String line : copied) {
                out.println(line);
            }
            player.message(ChatFormat.CYAN.concat("Text has been saved to " + fileName));
        }
        catch (IOException ioex) {
            player.notice("Failed to save text: " + ioex.getMessage());
            ret = false;
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
        return ret;
    }

    public final boolean equals(Object obj) {
        if (obj instanceof Player) {
            return player.equals(obj);
        }
        return this == obj;
    }
}
