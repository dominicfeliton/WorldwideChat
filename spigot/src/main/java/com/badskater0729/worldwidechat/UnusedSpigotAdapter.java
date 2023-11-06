package com.badskater0729.worldwidechat;
public class UnusedSpigotAdapter implements ServerAdapter {
    private final String version;

    public UnusedSpigotAdapter(String version) {
        this.version = version;
    }

    // TODO: IMPLEMENTATION AFTER PAPER
    @Override
    public String getVersion() {
        return version;
    }
}
