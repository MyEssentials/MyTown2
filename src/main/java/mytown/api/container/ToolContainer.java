package mytown.api.container;

import mytown.entities.tools.Tool;

public class ToolContainer {

    public Tool tool;

    public boolean exists() {
        return tool != null;
    }

    public void set(Tool tool) {
        this.tool = tool;
    }

    public void remove() {
        this.tool = null;
    }

    public Tool get() {
        return tool;
    }
}
