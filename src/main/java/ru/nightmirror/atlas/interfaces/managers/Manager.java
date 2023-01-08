package ru.nightmirror.atlas.interfaces.managers;

public interface Manager {
    Manager load();
    void stop();
    boolean reload();
}
