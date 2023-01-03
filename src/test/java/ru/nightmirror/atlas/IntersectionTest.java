package ru.nightmirror.atlas;

import org.junit.jupiter.api.Test;
import ru.nightmirror.atlas.intersection.IntersectionChecker;
import ru.nightmirror.atlas.intersection.exceptions.InvalidAreaException;
import ru.nightmirror.atlas.models.Area;
import ru.nightmirror.atlas.models.Line;
import ru.nightmirror.atlas.models.Point;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntersectionTest {

    private static final IntersectionChecker checker = new IntersectionChecker();

    @Test
    public void check_point_is_on_line() {
        Line l = new Line(new Point(-1, -2), new Point(3, -2));
        Point p1 = new Point(-1, -2);
        Point p2 = new Point(3, -2);
        Point p3 = new Point(0, -2);

        assertTrue(checker.isOnLine(l, p1) && checker.isOnLine(l, p2) && checker.isOnLine(l, p3));
    }

    @Test
    public void check_point_is_not_on_line() {
        Line l = new Line(new Point(-1, -2), new Point(3, -2));
        Point p1 = new Point(-1, -3);
        Point p2 = new Point(-2, 0);
        Point p3 = new Point(30, -30);

        assertTrue(!checker.isOnLine(l, p1) && !checker.isOnLine(l, p2) && !checker.isOnLine(l, p3));
    }

    @Test
    public void check_line_is_intersecting() {
        Line l1 = new Line(new Point(-1, -2), new Point(-1, 3));
        Line l2 = new Line(new Point(1, -3), new Point(-2, 1));

        assertTrue(checker.isLinesIntersect(l1, l2));
    }

    @Test
    public void check_line_is_not_intersecting() {
        Line l1 = new Line(new Point(-4, -4), new Point(-4, 1));
        Line l2 = new Line(new Point(-1, -2), new Point(3, -2));

        assertFalse(checker.isLinesIntersect(l1, l2));
    }

    @Test
    public void area_is_valid() {
        Area a = new Area(Set.of(new Point(-1, -2), new Point(3, -2), new Point(3, 3)));
        assertTrue(checker.isAreaValid(a));
    }

    @Test
    public void area_is_invalid() {
        Area a = new Area(Set.of(new Point(-1, -2), new Point(3, -2)));
        assertFalse(checker.isAreaValid(a));
    }

    @Test
    public void is_point_inside_area() throws InvalidAreaException {
        Area a = new Area(Set.of(new Point(-1, -2), new Point(3, -2), new Point(3, 3), new Point(-1, 3)));
        Point p1 = new Point(0, 0);
        Point p2 = new Point(-1, -2);
        assertTrue(checker.isPointInside(a, p1) && checker.isPointInside(a, p2));
    }

    @Test
    public void is_point_outside_area() throws InvalidAreaException {
        Area a = new Area(Set.of(new Point(-1, -2), new Point(3, -2), new Point(3, 3), new Point(-1, 3)));
        Point p = new Point(-2, -3);
        assertFalse(checker.isPointInside(a, p));
    }

    @Test
    public void is_areas_intersect() {
        Area a1 = new Area(Set.of(new Point(-1, -2), new Point(3, -2), new Point(3, 3), new Point(-1, 3)));
        Area a2 = new Area(Set.of(new Point(1, -3), new Point(2, 4), new Point(-2, 1)));

        assertTrue(checker.isAreaIntersect(a1, a2));
    }

    @Test
    public void is_areas_not_intersect() {
        Area a1 = new Area(Set.of(new Point(-1, -2), new Point(3, -2), new Point(3, 3), new Point(-1, 3)));
        Area a2 = new Area(Set.of(new Point(-4, -4), new Point(-1, -4), new Point(-4, 1)));

        assertFalse(checker.isAreaIntersect(a1, a2));
    }
}
