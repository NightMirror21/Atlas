package ru.nightmirror.atlas.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Line {
    private Point point1;
    private Point point2;
}
