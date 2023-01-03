package ru.nightmirror.atlas.interfaces.controllers;

public interface Controller {
    Controller load();
    void stop();
    boolean reload();
}
