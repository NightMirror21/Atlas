package ru.nightmirror.atlas.models;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Point {
    private int x;
    private int y; // this y is z in minecraft
}
