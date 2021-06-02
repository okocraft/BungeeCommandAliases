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

    public CommandConfig(Main plugin) {
        super(plugin, "commands.yml");
    }

    public List<String> getChildren(String alias) {
        return getAliasesMap().getOrDefault(
            alias.toLowerCase(Locale.ROOT),
            Collections.unmodifiableList(new ArrayList<>())
        );
    }

    public Map<String, List<String>> getAliasesMap() {
        Map<String, List<String>> result = new HashMap<>();
        Configuration aliasesSection = get().getSection("aliases");
        if (aliasesSection == null) {
            return result;
        }

        for (String alias : aliasesSection.getKeys()) {
            List<String> children = new ArrayList<>();
            
            Object value = aliasesSection.get(alias.toLowerCase(Locale.ROOT));

            if (value instanceof String) {
                children.add((String) value);

            } else if (value instanceof List) {
                for (Object element : (List<?>) value) {
                    if (element instanceof String) {
                        children.add((String) element);
                    }
                }
            }

            result.put(alias.toLowerCase(Locale.ROOT), Collections.unmodifiableList(children));
        }

        return result;
    }
}