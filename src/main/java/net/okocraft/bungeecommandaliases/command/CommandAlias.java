package net.okocraft.bungeecommandaliases.command;

import net.okocraft.bungeecommandaliases.Main;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandAlias extends Command implements TabExecutor {

    private final Main plugin;

    public CommandAlias(Main plugin, String alias) {
        super(alias);
        this.plugin = plugin;
    }

    private List<String> getReplacedChildren(String[] args, boolean forTabcompletion) throws IllegalArgumentException {
        List<String> children = new ArrayList<>(plugin.getCommandConfig().getChildren(getName()));
        if (children.isEmpty()) {
            return new ArrayList<>();
        }
        
        for (int childIndex = 0; childIndex < children.size(); childIndex++) {
            String child = children.get(childIndex);
            for (int argIndex = 0; argIndex < args.length; argIndex++) {
                // extract `$x-` to `$x $x+1-` or `$$x-` to `$$x $x+1-`
                // if it is for tabcomplete, even on the last argument, `$x+1-` will be added.
                if (forTabcompletion || argIndex != args.length - 1) {
                    child = child.replaceAll("(((?<!\\\\)\\$)\\$?" + (argIndex + 1) + ")-", "$1 $2" + (argIndex + 2) + "-");
                } else {
                    child = child.replaceAll("(((?<!\\\\)\\$\\$?)" + (argIndex + 1) + ")-", "$1");
                }
                
                // finally `$x`s are replaced with input args, but `$x-` are not.
                child = child.replaceAll("(?<!\\\\)\\$\\$?" + (argIndex + 1) + "(?!-)", args[argIndex]);
            }

            // if it is not tabcomplete and there are `$$x`s throw exception.
            if (!forTabcompletion && child.matches(".*(?<!\\\\)\\$\\$(\\d+).*")) {
                throw new IllegalArgumentException("Missing required argument " + child.replaceAll(".*(?<!\\\\)\\$\\$(\\d+).*", "$1"));
            }
            children.set(childIndex, child);
        }

        return children;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<String> children;
        try {
            children = getReplacedChildren(args, false);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(new TextComponent(e.getMessage()));
            return;
        }

        for (String child : children) {
            String command = child.replaceAll("(?<!\\\\)\\$(\\d+)-?", "");
            Main.debug(command);
            plugin.getProxy().getPluginManager().dispatchCommand(sender, command);
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completion = new ArrayList<>();

        for (String child : getReplacedChildren(Arrays.copyOfRange(args, 0, args.length - 1), true)) {
            plugin.getProxy().getPluginManager().dispatchCommand(
                sender,
                child.replaceAll("(?<!\\\\)\\$\\$?" + args.length + ".*", ""),
                completion
            );
        }

        return completion;
    }
}
