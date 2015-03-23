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

import net.canarymod.api.chat.ChatComponent;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;
import net.visualillusionsent.minecraft.plugin.ChatFormat;
import net.visualillusionsent.minecraft.plugin.canary.VisualIllusionsCanaryPluginInformationCommand;

import java.util.HashMap;

public final class SignEditListener extends VisualIllusionsCanaryPluginInformationCommand implements PluginListener {

    private final HashMap<Player, SignEditor> editors = new HashMap<Player, SignEditor>();

    public SignEditListener(SignEdit signedit) throws CommandDependencyException {
        super(signedit);
        signedit.registerListener(this);
        signedit.registerCommands(this, false);
    }

    @HookHandler(priority = Priority.HIGH)
    public void onSignEdit(BlockRightClickHook hook) {
        if (isSign(hook.getBlockClicked().getType())) {
            Sign sign = (Sign) hook.getBlockClicked().getTileEntity();
            Player player = hook.getPlayer();
            if (isEditing(player)) {
                if (isCopying(player)) {
                    editors.get(player).storeCopied(sign.getLines());
                    player.message(ChatFormat.CYAN.concat("Text copied, paste to other signs with /signedit paste"));
                }
                else if (sign.isEditable() || player.equals(sign.getOwner()) || player.hasPermission("signedit.editall")) {
                    if (isPasting(player)) {
                        //sign.setText(editors.get(player).getCopied());
                        sign.setComponents(editors.get(player).getCopied());
                        sign.update();
                    }
                    else {
                        sign.setEditable(true);
                        player.openSignEditWindow(sign);
                    }
                }
                else {
                    player.notice("You aren't allowed to edit that sign...");
                }
                if (!isPersistantEditing(player)) {
                    editors.get(player).allOff();
                }
                hook.setCanceled();
            }
        }
    }

    @HookHandler
    public final void onSignChange(SignChangeHook hook) {
        if (hook.getPlayer().hasPermission("signedit.colors")) {
            ChatComponent[] text = hook.getSign().getLines();
            for (int index = 0; index < 4; index++) {
                text[index] = SignEdit.newComponent(text[index].getFullText().replaceAll("&([0-9A-FK-NRa-fk-nr])", "\u00A7$1"));
            }
            hook.getSign().setComponents(text);
            hook.getSign().setEditable(false);
        }
    }

    @HookHandler
    public final void onPlayerGone(DisconnectionHook hook) {
        editors.remove(hook.getPlayer()); //Garbage collect
    }

    @Command(
            aliases = { "signedit" },
            description = "Sign Edit activation command",
            permissions = { "signedit.edit" },
            toolTip = "/signedit [copy|paste|save|load|persist|off]"
    )
    public final void editCommand(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            addGetEditing((Player) msgrec).enableEditing();
            msgrec.message(ChatFormat.ORANGE + "Right-Click a sign to edit...");
        }
        else {
            msgrec.notice("Only Players can edit signs.");
        }
    }

    @Command(
            aliases = { "-p", "persist" },
            description = "SignEdit Persistance Setting",
            permissions = { "signedit.edit.persist" },
            toolTip = "/signedit persist",
            parent = "signedit"
    )
    public final void persist(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            addGetEditing((Player) msgrec).enableEditing().enablePersistance();
            msgrec.message(ChatFormat.ORANGE + "Right-Click a sign to edit...");
        }
        else {
            msgrec.notice("Only Players can edit signs.");
        }
    }

    @Command(
            aliases = { "paste" },
            description = "Paste text to a sign",
            permissions = { "signedit.edit.paste" },
            toolTip = "/signedit paste [persist]",
            parent = "signedit"
    )
    public final void pasteText(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            if (hasCopiedText((Player) msgrec)) {
                if (args.length > 1 && args[1].toLowerCase().matches("(\\-p|persist)") && msgrec.hasPermission("signedit.edit.persist")) {
                    editors.get(msgrec).enablePersistance();
                }
                editors.get(msgrec).enablePasting();
                msgrec.message(ChatFormat.ORANGE + "Right-Click a sign to paste text to...");
            }
            else {
                msgrec.notice("You need to load or copy text before pasting");
            }
        }
        else {
            msgrec.notice("Only Players can save sign text.");
        }
    }

    @Command(
            aliases = { "copy" },
            description = "Copies sign text",
            permissions = { "signedit.edit.copy" },
            toolTip = "/signedit copy",
            parent = "signedit"
    )
    public final void copyText(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            addGetEditing((Player) msgrec).enableCopying();
            msgrec.message(ChatFormat.ORANGE + "Right-Click a sign to copy text from...");
        }
        else {
            msgrec.notice("Only Players can copy sign text.");
        }
    }

    @Command(
            aliases = { "save" },
            description = "Saves sign text to a file",
            permissions = { "signedit.edit.save" },
            toolTip = "/signedit save <name>",
            parent = "signedit",
            min = 2
    )
    public final void saveText(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            if (hasCopiedText((Player) msgrec)) {
                editors.get(msgrec).saveSignText(args[1]);
            }
        }
        else {
            msgrec.notice("Only Players can save sign text.");
        }
    }

    @Command(
            aliases = { "load" },
            description = "Loads sign text from a file",
            permissions = { "signedit.edit.load" },
            toolTip = "/signedit load <name>",
            parent = "signedit",
            min = 2
    )
    public final void loadText(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player) {
            addGetEditing((Player) msgrec).loadSignText(args[1]);
        }
        else {
            msgrec.notice("Only Players can save sign text.");
        }
    }

    @Command(
            aliases = { "off" },
            description = "Turns off all SignEdit things",
            permissions = { "signedit.edit" },
            toolTip = "/signedit off",
            parent = "signedit"
    )
    public final void editOff(MessageReceiver msgrec, String[] args) {
        if (msgrec instanceof Player && editors.containsKey(msgrec)) {
            editors.get(msgrec).allOff();
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
        return editors.containsKey(player) && editors.get(player).isEditing();
    }

    private boolean isPersistantEditing(Player player) {
        return editors.containsKey(player) && editors.get(player).isPersistent();
    }

    private boolean isCopying(Player player) {
        return editors.containsKey(player) && editors.get(player).isCopying();
    }

    private boolean isPasting(Player player) {
        return editors.containsKey(player) && editors.get(player).isPasting();
    }

    private boolean hasCopiedText(Player player) {
        return editors.containsKey(player) && editors.get(player).getCopied() != null;
    }

    private SignEditor addGetEditing(Player player) {
        if (!editors.containsKey(player)) {
            editors.put(player, new SignEditor(player));
        }
        return editors.get(player);
    }
}
