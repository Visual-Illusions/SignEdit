/*
 * This file is part of SignEdit.
 *
 * Copyright © 2013-2015 Visual Illusions Entertainment
 *
 * SignEdit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License v3 for more details.
 *
 * You should have received a copy of the GNU General Public License v3 along with this program.
 * If not, see http://www.gnu.org/licenses/gpl.html.
 */
package net.visualillusionsent.signedit;

import net.canarymod.Canary;
import net.canarymod.api.chat.ChatComponent;
import net.canarymod.api.factory.ChatComponentFactory;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPlugin;

public final class SignEdit extends VisualIllusionsCanaryPlugin {
    private static final ChatComponentFactory componentFactory = Canary.factory().getChatComponentFactory();

    @Override
    public final boolean enable() {
        super.enable();
        try {
            new SignEditListener(this);
        }
        catch (Exception ex) {
            getLogman().warn("SignEdit failed to enable...", ex);
            return false;
        }
        return true;
    }

    @Override
    public final void disable() {
    }

    public static ChatComponent newComponent(String text) {
        return componentFactory.newChatComponent(text);
    }
}
