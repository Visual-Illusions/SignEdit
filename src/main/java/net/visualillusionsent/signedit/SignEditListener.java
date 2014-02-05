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
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import java.util.HashMap;

public final class SignEditListener extends VisualIllusionsCanaryPluginInformationCommand implements PluginListener {

    private final SignEdit signedit;
    private final HashMap<Player, Boolean> editors = new HashMap<Player, Boolean>();

    public SignEditListener(SignEdit signedit) throws CommandDependencyException {
        super(signedit);
        signedit.registerListener(this);
        signedit.registerCommands(this, false);
        this.signedit = signedit;
    }

    @HookHandler(priority = Priority.HIGH)
    public void onSignEdit(BlockRightClickHook hook) {
        if (isSign(hook.getBlockClicked().getType())) {
            Sign sign = (Sign) hook.getBlockClicked().getTileEntity();
            Player player = hook.getPlayer();
            if (isEditing(player)) {
                if (sign.isEditable() || player.equals(sign.getOwner()) || player.hasPermission("signedit.editall")) {
                    sign.setEditable(true);
                    player.openSignEditWindow(sign);
                }
                else {
                    player.notice("You aren't allowed to edit that sign...");
                }
                if (!isPersistantEditing(player)) {
                    editors.remove(player);
                }
                hook.setCanceled();
            }
        }
    }

    @HookHandler
    public final void onSignChange(SignChangeHook hook) {
        if (hook.getPlayer().hasPermission("signedit.colors")) {
            String[] text = hook.getSign().getText();
            for (int index = 0; index < 4; index++) {
                text[index] = text[index].replaceAll("&([0-9A-FK-NRa-fk-nr])", "\u00A7$1");
            }
            hook.getSign().setText(text);
            hook.getSign().setEditable(false);
        }
    }

    @HookHandler
    public final void onPlayerGone(DisconnectionHook hook) {
        editors.remove(hook.getPlayer());
    }

    @Command(
            aliases = { "signedit" },
            description = "Sign Edit activation command",
            permissions = { "signedit.edit" },
            toolTip = "/signedit [persist]"
    )
    public final void editCommand(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            addEditing((Player) msgrec, args.length > 1 && args[1].equals("persist"));
            msgrec.message(TextFormat.ORANGE + "Right-Click a sign to edit...");
        }
        else {
            msgrec.notice("Only Players can edit signs.");
        }
    }

    @Command(
            aliases = { "info" },
            description = "SignEdit Information Command",
            permissions = { "signedit" },
            toolTip = "/signedit info",
            parent = "signedit"
    )
    public final void infoCommand(MessageReceiver msgrec, String[] args) {
        super.sendInformation(msgrec);
    }

    private boolean isSign(BlockType type) {
        return type == BlockType.WallSign || type == BlockType.SignPost;
    }

    private boolean isEditing(Player player) {
        return editors.containsKey(player);
    }

    private boolean isPersistantEditing(Player player) {
        return editors.get(player);
    }

    private void addEditing(Player player, boolean persist) {
        editors.put(player, persist);
    }
}
