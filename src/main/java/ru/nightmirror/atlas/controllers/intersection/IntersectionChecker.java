package ru.nightmirror.atlas.controllers.intersection;

import ru.nightmirror.atlas.controllers.intersection.exceptions.InvalidAreaException;
import ru.nightmirror.atlas.models.Area;
import ru.nightmirror.atlas.models.Line;
import ru.nightmirror.atlas.models.Point;

import java.util.List;

public class IntersectionChecker {

    public boolean isOnLine(Line l, Point p) {
        return (p.getX() <= Math.max(l.getPoint1().getX(), l.getPoint2().getX())
                && p.getX() >= Math.min(l.getPoint1().getX(), l.getPoint2().getX())
                && p.getY() <= Math.max(l.getPoint1().getY(), l.getPoint2().getY())
                && p.getY() >= Math.min(l.getPoint1().getY(), l.getPoint2().getY()));
    }

    private int getDirection(Point p1, Point p2, Point p3) {
        final int direction = (p2.getY() - p1.getY()) * (p3.getX() - p2.getX()) - (p2.getX() - p1.getX()) * (p3.getY() - p2.getY());

        if (direction == 0) {
            return 0;
        } else if (direction < 0) {
            return 2;
        }

        return 1;
    }

    public boolean isLinesIntersect(Line l1, Line l2) {
        final int dir1 = getDirection(l1.getPoint1(), l1.getPoint2(), l2.getPoint1());
        final int dir2 = getDirection(l1.getPoint1(), l1.getPoint2(), l2.getPoint2());
        final int dir3 = getDirection(l2.getPoint1(), l2.getPoint2(), l1.getPoint1());
        final int dir4 = getDirection(l2.getPoint1(), l2.getPoint2(), l1.getPoint2());

        return  ((dir1 != dir2 && dir3 != dir4)
                || (dir1 == 0 && isOnLine(l1, l2.getPoint1()))
                || (dir2 == 0 && isOnLine(l1, l2.getPoint2()))
                || (dir3 == 0 && isOnLine(l2, l1.getPoint1()))
                || (dir4 == 0 && isOnLine(l2, l1.getPoint2())));
    }

    public boolean isAreaValid(Area a) {
        return a.countOfPoints() >= 3;
    }

    public boolean isPointInside(Area a, Point p) throws InvalidAreaException {
        if (a.countOfPoints() < 3) throw new InvalidAreaException(String.format("Need 3 or more points, but found %d", a.countOfPoints()));

        Line exline = new Line(p, new Point(99999999, p.getY()));
        List<Point> points = a.getPointsList();

        int count = 0;
        int i = 0;

        do {
            Line side = new Line(points.get(i), points.get((i + 1) % points.size()));
            if (isLinesIntersect(side, exline)) {
                if (getDirection(side.getPoint1(), p, side.getPoint2()) == 0) {
                    return isOnLine(side, p);
                }
                count++;
            }
            i = (i + 1) % points.size();
        } while (i != 0);

        return count % 2 != 0;
    }

    public boolean isAreaIntersect(Area a1, Area a2) {
        List<Line> linesA1 = a1.getLines();
        List<Line> linesA2 = a2.getLines();

        for (int i = 0; i < linesA1.size(); i++) {
            for (int j = i+1; j < linesA2.size(); j++) {
                if (isLinesIntersect(linesA1.get(i), linesA2.get(j))) {
                    return true;
                }
            }
        }

        return false;
    }
}
