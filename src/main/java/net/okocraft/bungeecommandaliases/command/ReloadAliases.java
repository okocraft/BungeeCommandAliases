package net.okocraft.bungeecommandaliases.command;

import java.util.Objects;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.okocraft.bungeecommandaliases.Main;

public class ReloadAliases extends Command {

    private final Main plugin;

    public ReloadAliases(Main plugin) {
        super("reloadaliases", "bungeecommandaliases.reload");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (plugin.isAliasesLoaded()) {
            plugin.onDisable();
            plugin.onEnable();
            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Bungeecord command aliases successfully reloaded."));
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Bungeecord command aliases is not loaded yet."));
        }
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ReloadAliases)) {
            return false;
        }
        ReloadAliases reloadAliases = (ReloadAliases) o;
        return Objects.equals(plugin, reloadAliases.plugin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(plugin);
    }

    
}
