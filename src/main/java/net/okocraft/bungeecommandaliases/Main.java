package net.okocraft.bungeecommandaliases;

import net.okocraft.bungeecommandaliases.command.CommandAlias;
import net.okocraft.bungeecommandaliases.config.CommandConfig;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Multimap;

import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class Main extends Plugin {

    private final CommandConfig commandConfig;

    private final Map<String, CommandAlias> commandAliases = new HashMap<>();
    private final Map<String, Map.Entry<Plugin, Command>> conflictingCommands = new HashMap<>();

    private Plugin getOwningPlugin(Command command) {
        try {
            Field commandByPlugin = PluginManager.class.getDeclaredField("commandByPlugin");
            commandByPlugin.setAccessible(true);
            @SuppressWarnings("unchecked")
            Multimap<Plugin, Command> commandByPluginMap = (Multimap<Plugin, Command>) commandByPlugin.get(getProxy().getPluginManager());
            for (Map.Entry<Plugin, Command> entry : commandByPluginMap.entries()) {
                if (entry.getValue().equals(command)) {
                    return entry.getKey();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }
    

    public Main() {
        commandConfig = new CommandConfig(this);
    }

    @Override
    public void onEnable() {
        Map<String, Command> registeredCommands = new HashMap<>();
        for (Map.Entry<String, Command> entry : getProxy().getPluginManager().getCommands()) {
            registeredCommands.put(entry.getKey(), entry.getValue());
        }

        commandConfig.saveDefault();
        for (String aliasName : commandConfig.getCommandAliasesMap().keySet()) {
            CommandAlias alias = new CommandAlias(this, aliasName);
            commandAliases.put(aliasName, alias);
            if (registeredCommands.containsKey(aliasName)) {
                conflictingCommands.put(
                    aliasName,
                    Map.entry(getOwningPlugin(registeredCommands.get(aliasName)), registeredCommands.get(aliasName))
                );
            }
            getProxy().getPluginManager().registerCommand(this, alias);
        }

        // prevent confliction with other plugin command with the same name.
        getProxy().getScheduler().schedule(this, () -> {
            commandAliases.forEach((aliasName, alias) -> getProxy().getPluginManager().registerCommand(this, alias));
        }, 20L, TimeUnit.SECONDS);
    }

    public void disableAlias(String aliasName) {
        getProxy().getPluginManager().unregisterCommand(commandAliases.get(aliasName));
        if (conflictingCommands.containsKey(aliasName)) {
            Plugin owner = conflictingCommands.get(aliasName).getKey();
            if (getProxy().getPluginManager().getPlugins().contains(owner)) {
                Command command = conflictingCommands.get(aliasName).getValue();
                getProxy().getPluginManager().registerCommand(owner, command);
            }
        }
    }

    @Override
    public void onDisable() {
        commandAliases.keySet().forEach(this::disableAlias);
    }

    public CommandConfig getCommandConfig() {
        return commandConfig;
    }
}