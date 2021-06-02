package net.okocraft.bungeecommandaliases.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.md_5.bungee.config.Configuration;
import net.okocraft.bungeecommandaliases.Main;

public class CommandConfig extends CustomConfig {

    private Map<String, List<String>> aliasMap;

    public CommandConfig(Main plugin) {
        super(plugin, "commands.yml");
    }

    public List<String> getAliases(String command) {
        return getCommandAliasesMap().getOrDefault(
            command.toLowerCase(Locale.ROOT),
            Collections.unmodifiableList(new ArrayList<>())
        );
    }

    public Map<String, List<String>> getCommandAliasesMap() {
        if (aliasMap != null) {
            return aliasMap;
        }

        Map<String, List<String>> result = new HashMap<>();
        Configuration aliasesSection = get().getSection("aliases");
        if (aliasesSection == null) {
            return result;
        }

        for (String command : aliasesSection.getKeys()) {
            List<String> aliases = new ArrayList<>();
            
            Object alias = aliasesSection.get(command.toLowerCase(Locale.ROOT));

            if (alias instanceof String) {
                aliases.add((String) alias);

            } else if (alias instanceof List) {
                for (Object element : (List<?>) alias) {
                    if (element instanceof String) {
                        aliases.add((String) element);
                    }
                }
            }

            result.put(command.toLowerCase(Locale.ROOT), Collections.unmodifiableList(aliases));
        }

        aliasMap = result;
        return aliasMap;
    }
}