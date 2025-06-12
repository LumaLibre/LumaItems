package dev.jsinco.luma.lumaitems.util;

import org.bukkit.entity.Entity;

@FunctionalInterface
public interface EntityCallBack {
    void go(Entity entity);
}
