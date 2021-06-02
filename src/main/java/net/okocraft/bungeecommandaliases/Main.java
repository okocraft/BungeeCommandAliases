package net.okocraft.bungeecommandaliases;

import net.okocraft.bungeecommandaliases.command.CommandAlias;
import net.okocraft.bungeecommandaliases.command.ReloadAliases;
import net.okocraft.bungeecommandaliases.config.CommandConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Multimap;

import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class Main extends Plugin {

    private boolean aliasesLoaded = false;

    private final CommandConfig commandConfig;

    private final ReloadAliases reloadCommand;

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
    
    public boolean isAliasesLoaded() {
        return aliasesLoaded;
    }

    public Main() {
        commandConfig = new CommandConfig(this);
        reloadCommand = new ReloadAliases(this);
    }

    @Override
    public void onEnable() {
        Map<String, Command> registeredCommands = new HashMap<>();
        for (Map.Entry<String, Command> entry : getProxy().getPluginManager().getCommands()) {
            registeredCommands.put(entry.getKey(), entry.getValue());
        }

        if (!registeredCommands.containsValue(reloadCommand)) {
            getProxy().getPluginManager().registerCommand(this, reloadCommand);
        }

        commandConfig.saveDefault();
        Map<String, List<String>> aliasesMap = commandConfig.getAliasesMap();
        for (String aliasName : aliasesMap.keySet()) {
            CommandAlias alias = new CommandAlias(this, aliasName);
            commandAliases.put(aliasName, alias);
            if (registeredCommands.containsKey(aliasName)) {
                conflictingCommands.put(
                    aliasName,
                    Map.entry(getOwningPlugin(registeredCommands.get(aliasName)), registeredCommands.get(aliasName))
                );
            }
        }

        Map<String, List<String>> invalidAliasesAndChildren = new HashMap<>();
        for (String aliasName : commandAliases.keySet()) {
            List<String> invalidChildren = new ArrayList<>();
            invalidChildren.addAll(getLoopingRecursiveAlias(aliasName, aliasesMap));
            
            List<String> childNames = new ArrayList<>(aliasesMap.get(aliasName));
            childNames.replaceAll(child -> child.split(" ", -1)[0]);
            invalidChildren.retainAll(childNames);
            invalidChildren.remove(aliasName);

            for (String child : aliasesMap.get(aliasName)) {
                String childName = child.split(" ", -1)[0];
                if (!invalidChildren.contains(childName) && !registeredCommands.containsKey(childName)) {
                    invalidChildren.add(childName);
                }
            }

            if (!invalidChildren.isEmpty()) {
                invalidAliasesAndChildren.put(aliasName, invalidChildren);
            }
        }
        for (String invalidAlias : invalidAliasesAndChildren.keySet()) {
            String invalidChildren = String.join(", ", invalidAliasesAndChildren.get(invalidAlias).toArray(String[]::new));
            getLogger().warning("Could not register alias " + invalidAlias + " because it contains commands that do not exist: " + invalidChildren);
            commandAliases.remove(invalidAlias);
        }

        if (aliasesLoaded) {
            commandAliases.forEach((aliasName, alias) -> getProxy().getPluginManager().registerCommand(this, alias));
        } else {
            // prevent confliction with other plugin command with the same name.
            getProxy().getScheduler().schedule(this, () -> {
                    commandAliases.forEach((aliasName, alias) -> getProxy().getPluginManager().registerCommand(this, alias));
            }, 10L, TimeUnit.SECONDS);
            aliasesLoaded = true;
        }
    }

    private List<String> getLoopingRecursiveAlias(String aliasName, Map<String, List<String>> aliasesMap) {
        return getLoopingRecursiveAlias0(aliasName, new ArrayList<>(), -1, aliasesMap);
    }

    private List<String> getLoopingRecursiveAlias0(String aliasName, List<String> parents, int lastJunctionIndex, Map<String, List<String>> aliasesMap) {
        List<String> children = new ArrayList<>(aliasesMap.get(aliasName));
        children.replaceAll(child -> child.split(" ", -1)[0]);
        children.removeIf(child -> !aliasesMap.containsKey(child));

        if (!children.isEmpty()) {
            if (parents.contains(aliasName)) {
                return parents;
            }
            parents.add(aliasName);
            if (children.size() > 1) {
                lastJunctionIndex = parents.size() - 1;
            }
            aliasName = children.get(0);

        } else {
            if (lastJunctionIndex == -1) {
                return new ArrayList<>();
            }

            children = new ArrayList<>(aliasesMap.get(parents.get(lastJunctionIndex)));
            children.replaceAll(child -> child.split(" ", -1)[0]);
            children.removeIf(child -> !aliasesMap.containsKey(child));
            children.remove(parents.get(lastJunctionIndex + 1));

            aliasName = children.get(0);
            parents = new ArrayList<>(parents.subList(0, lastJunctionIndex + 1));
        }

        return getLoopingRecursiveAlias0(aliasName, parents, lastJunctionIndex, aliasesMap);
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
        commandAliases.clear();
        conflictingCommands.clear();
        
    }

    public CommandConfig getCommandConfig() {
        return commandConfig;
    }
}