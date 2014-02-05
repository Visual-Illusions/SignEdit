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
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPlugin;

import java.util.ArrayList;

public final class SignEdit extends VisualIllusionsCanaryPlugin {

    private final ArrayList<Player> editors = new ArrayList<Player>();

    @Override
    public final boolean enable() {
        try {
            new SignEditListener(this);
        }
        catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    public final void disable() {
    }

    final boolean isEditing(Player player) {
        if (editors.contains(player)) {
            return editors.remove(player);
        }
        return false;
    }

    final boolean addEditing(Player player) {
        if (editors.contains(player)) {
            return false;
        }
        return editors.add(player);
    }
}
