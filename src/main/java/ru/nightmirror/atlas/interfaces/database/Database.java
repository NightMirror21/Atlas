package ru.nightmirror.atlas.interfaces.database;

public interface Database extends IDatabaseLoader {
    boolean connect();
    void close();
    boolean reload();
}
