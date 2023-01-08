package ru.nightmirror.atlas.interfaces.controllers;

import java.util.UUID;

public interface IPlayerController {
    void removeAllCallbacks(UUID uuid);
    boolean containsAnyCallback(UUID uuid);
}
