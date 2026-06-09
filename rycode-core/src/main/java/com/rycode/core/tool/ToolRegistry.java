package com.rycode.core.tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ryan
 */
public final class ToolRegistry {

    private final List<Tool> tools;
    private final Map<String, Tool> toolsByName;

    public ToolRegistry(List<Tool> tools) {
        this.tools = List.copyOf(Objects.requireNonNull(tools, "tools must not be null"));
        this.toolsByName = this.tools.stream()
                .collect(Collectors.toUnmodifiableMap(
                        tool -> tool.definition().name(),
                        Function.identity()
                ));
    }

    public Optional<Tool> findByName(String name) {
        return Optional.ofNullable(toolsByName.get(name));
    }

    public List<ToolDefinition> definitions() {
        return tools.stream()
                .map(Tool::definition)
                .toList();
    }
}
