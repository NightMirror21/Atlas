package ru.nightmirror.atlas.misc.type;

import lombok.Data;

@Data
public class Type {
    private final String name;
    private final Color color;

    public Type(String name, String color) {
        this.name = name;
        this.color = Color.parse(color);
    }
}
