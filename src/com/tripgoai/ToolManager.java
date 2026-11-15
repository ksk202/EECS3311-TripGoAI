package com.tripgoai;

import java.util.HashMap;
import java.util.Map;

public class ToolManager {

    private final Map<String, TravelTool> tools = new HashMap<>();

    public void registerTool(TravelTool tool) {
        tools.put(tool.getName(), tool);
    }

    public String executeTool(String toolName, String input) {
        TravelTool tool = tools.get(toolName);

        if (tool == null) {
            return "Tool not found: " + toolName;
        }

        return tool.execute(input);
    }

    public int getToolCount() {
        return tools.size();
    }
}