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

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import java.util.ArrayList;

public final class SignEditListener extends VisualIllusionsCanaryPluginInformationCommand implements PluginListener {

    private final SignEdit signedit;
    private final ArrayList<Player> persistant = new ArrayList<Player>();

    public SignEditListener(SignEdit signedit) throws CommandDependencyException {
        super(signedit);
        Canary.hooks().registerListener(this, signedit);
        Canary.commands().registerCommands(this, signedit, false);
        this.signedit = signedit;
    }

    @HookHandler(priority = Priority.HIGH)
    public void onSignEdit(BlockRightClickHook hook) {
        if (isSign(hook.getBlockClicked().getType())) {
            if (signedit.isEditing(hook.getPlayer())) {
                Sign sign = (Sign) hook.getBlockClicked().getTileEntity();
                if (sign.isEditable() || sign.getOwner() == hook.getPlayer() || hook.getPlayer().hasPermission("signedit.editall")) {
                    sign.setEditable(true);
                    hook.getPlayer().openSignEditWindow(sign);
                    hook.setCanceled();
                }
                else {
                    hook.getPlayer().notice("You aren't allowed to edit that sign...");
                }
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

    @Command(aliases = { "signedit" },
            description = "Sign Edit activation command",
            permissions = { "signedit.edit" },
            toolTip = "/signedit")
    public final void editCommand(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            signedit.addEditing((Player) msgrec);
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
            toolTip = "/signedit info"
    )
    public final void infoCommand(MessageReceiver msgrec, String[] args) {
        super.sendInformation(msgrec);
    }

    private boolean isSign(BlockType type) {
        return type == BlockType.WallSign || type == BlockType.SignPost;
    }
}
