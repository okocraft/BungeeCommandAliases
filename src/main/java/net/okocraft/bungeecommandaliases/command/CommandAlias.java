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

    private List<String> getReplacedCommands(String[] args, boolean forTabcompletion) throws IllegalArgumentException {
        List<String> realCommands = new ArrayList<>(plugin.getCommandConfig().getAliases(getName()));
        if (realCommands.isEmpty()) {
            return new ArrayList<>();
        }
        
        for (int realCommandIndex = 0; realCommandIndex < realCommands.size(); realCommandIndex++) {
            String realCommand = realCommands.get(realCommandIndex);
            for (int argIndex = 0; argIndex < args.length; argIndex++) {
                // extract `$x-` to `$x $x+1-` or `$$x-` to `$$x $x+1-`
                // if it is for tabcomplete, even on the last argument, `$x+1-` will be added.
                if (forTabcompletion || argIndex != args.length - 1) {
                    realCommand = realCommand.replaceAll("(((?<!\\\\)\\$)\\$?" + (argIndex + 1) + ")-", "$1 $2" + (argIndex + 2) + "-");
                } else {
                    realCommand = realCommand.replaceAll("(((?<!\\\\)\\$\\$?)" + (argIndex + 1) + ")-", "$1");
                }
                
                // finally `$x`s are replaced with input args, but `$x-` are not.
                realCommand = realCommand.replaceAll("(?<!\\\\)\\$\\$?" + (argIndex + 1) + "(?!-)", args[argIndex]);
            }

            // if it is not tabcomplete and there are `$$x`s throw exception.
            if (!forTabcompletion && realCommand.matches(".*(?<!\\\\)\\$\\$(\\d+).*")) {
                throw new IllegalArgumentException("Missing required argument " + realCommand.replaceAll(".*(?<!\\\\)\\$\\$(\\d+).*", "$1"));
            }
            realCommands.set(realCommandIndex, realCommand);
        }

        return realCommands;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<String> commandLines;
        try {
            commandLines = getReplacedCommands(args, false);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(new TextComponent(e.getMessage()));
            return;
        }

        for (String commandLine : commandLines) {
            if (commandLine.split(" ", -1)[0].equals(getName())) {
                throw new IllegalStateException("Alias is looping: " + getName());
            } else {
                plugin.getProxy().getPluginManager().dispatchCommand(sender, commandLine);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> completion = new ArrayList<>();

        for (String commandLine : getReplacedCommands(Arrays.copyOfRange(args, 0, args.length - 1), true)) {
            if (!commandLine.split(" ", -1)[0].equals(getName())) {
                plugin.getProxy().getPluginManager().dispatchCommand(
                    sender,
                    commandLine.replaceAll("(?<!\\\\)\\$\\$?" + args.length + ".*", ""),
                    completion
                );
            }
        }

        return completion;
    }
}
