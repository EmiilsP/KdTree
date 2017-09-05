import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.SET;

import java.lang.*;
import java.util.Comparator;
import java.util.Stack;

public class PointSET {
    private SET<Point2D> points;

    public PointSET() {  // construct an empty set of points
        points = new SET<>();
    }

    public boolean isEmpty() {   // is the set empty?
        return points.isEmpty();
    }

    public int size() {   // number of points in the set
        return points.size();
    }

    public void insert(Point2D p) {    // add the point to the set (if it is not already in the set)
        if (p == null) {
            throw new NullPointerException();
        }
        points.add(p);
    }

    public boolean contains(Point2D p) {  // does the set contain point p?
        if (p == null) {
            throw new NullPointerException();
        }
        return points.contains(p);
    }

    public void draw() {  // draw all points to standard draw
        for (Point2D point : points) {
            point.draw();
        }
    }

    public Iterable<Point2D> range(RectHV rect) {   // all points that are inside the rectangle (or on the boundary)
        if (rect == null) {
            throw new NullPointerException();
        }
        Stack<Point2D> stack = new Stack<>();
        for (Point2D point : points) {
            if (rect.contains(point)) {
                stack.push(point);
            }
        }
        return stack;
    }

    public Point2D nearest(Point2D p) {   // a nearest neighbor in the set to point p; null if the set is empty
        if (p == null) {
            throw new NullPointerException();
        }
        if (this.size() == 0) {
            return null;
        }

        Comparator<Point2D> dComparator = new Distance(p);
        MinPQ<Point2D> pq = new MinPQ<Point2D>(this.size(), dComparator);

        for (Point2D point : points) {
            pq.insert(point);
        }
        return pq.min();
    }

    private class Distance implements Comparator<Point2D> {
        private Point2D point;

        public Distance(Point2D point) {
            this.point = point;
        }

        public int compare(Point2D p1, Point2D p2) {
            double dist1 = point.distanceSquaredTo(p1);
            double dist2 = point.distanceSquaredTo(p2);

            if (dist1 > dist2) {
                return +1;
            } else if (dist1 < dist2) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}