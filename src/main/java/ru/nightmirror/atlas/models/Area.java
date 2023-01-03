package ru.nightmirror.atlas.models;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class Area {
    private Set<Point> points;

    public void addPoint(Point point) {
        points.add(point);
    }

    public void removePoint(Point point) {
        points.remove(point);
    }

    public boolean containsPoint(Point point) {
        return points.contains(point);
    }

    public List<Point> getPointsList() {
        return new ArrayList<>(points);
    }

    public int countOfPoints() {
        return points.size();
    }

    public List<Line> getLines() {
        List<Line> lines = new ArrayList<>();

        for (int i = 0; i < countOfPoints(); i++) {
            for (int j = i+1; j < countOfPoints(); j++) {
                lines.add(new Line(getPointsList().get(i), getPointsList().get(j)));
            }
        }

        return lines;
    }
}
