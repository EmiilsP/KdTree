import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.Comparator;
import java.util.Stack;

public class KdTree {
    private static class Node { // node object
        private Point2D point;
        private RectHV rect;
        private Node left;
        private Node right;

        private Node(Point2D point, RectHV rect) {
            this.point = point;
            this.rect = rect;
        }
    }

    private Node root;
    private int size;

    public KdTree() {
        size = 0;
    }

    public boolean isEmpty() { // is KdTree empty?
        return size == 0;
    }

    public int size() { // size of KdTree
        return size;
    }

    public void insert(Point2D point) { // insert new point
        if (point == null) {
            throw new IllegalArgumentException();
        }
        root = insert(root, point, true, null, 0);
    }

    private Node insert(Node node, Point2D point, boolean side, Node parent, int x) {
        if (node == null) {
            RectHV rect;
            if (parent == null) {
                rect = new RectHV(0.0, 0.0, 1.0, 1.0);
            } else {
                if (!side) {
                    if (x < 0) {
                        rect = new RectHV(parent.rect.xmin(), parent.rect.ymin(), parent.point.x(), parent.rect.ymax());
                    } else {
                        rect = new RectHV(parent.point.x(), parent.rect.ymin(), parent.rect.xmax(), parent.rect.ymax());
                    }
                } else {
                    if (x < 0) {
                        rect = new RectHV(parent.rect.xmin(), parent.rect.ymin(), parent.rect.xmax(), parent.point.y());
                    } else {
                        rect = new RectHV(parent.rect.xmin(), parent.point.y(), parent.rect.xmax(), parent.rect.ymax());
                    }
                }
            }
            size++;
            return new Node(point, rect);
        }

        Comparator<Point2D> comparator;

        if (side) {
            comparator = Point2D.X_ORDER;
        } else {
            comparator = Point2D.Y_ORDER;
        }

        int comp = comparator.compare(point, node.point);

        if (comp < 0) {
            node.left = insert(node.left, point, !side, node, comp);
        } else if (comp > 0 || !node.point.equals(point)) {
            node.right = insert(node.right, point, !side, node, comp);
        }
        return node;
    }

    public boolean contains(Point2D point) {
        if (point == null) {
            throw new IllegalArgumentException();
        }
        return contains(root, point, true);
    }

    private boolean contains(Node node, Point2D point, boolean side) {
        if (node == null) {
            return false;
        }

        if (node.point.equals(point)) {
            return true;
        }

        Comparator<Point2D> comparator;

        if (side) {
            comparator = Point2D.X_ORDER;
        } else {
            comparator = Point2D.Y_ORDER;
        }

        int comp = comparator.compare(point, node.point);

        if (comp < 0) {
            return contains(node.left, point, !side);
        } else {
            return contains(node.right, point, !side);
        }
    }

    private static class Split {
        private Node node;
        private boolean side;

        private Split(Node node, Boolean side) {
            this.node = node;
            this.side = side;
        }
    }

    public void draw() { //draw all points
        if (root == null) {
            return;
        }

        Stack<Split> nodes = new Stack<Split>();

        nodes.push(new Split(root, true));

        while (!nodes.isEmpty()) {
            Split ns = nodes.pop();

            StdDraw.setPenRadius(.01);
            StdDraw.setPenColor(StdDraw.BLACK);
            ns.node.point.draw();

            RectHV r = ns.node.rect;
            StdDraw.setPenRadius();

            if (ns.side) {
                StdDraw.setPenColor(StdDraw.RED);
                StdDraw.line(ns.node.point.x(), r.ymin(), ns.node.point.x(), r.ymax());
            } else {
                StdDraw.setPenColor(StdDraw.BLUE);
                StdDraw.line(r.xmin(), ns.node.point.y(), r.xmax(), ns.node.point.y());
            }

            if (ns.node.left != null) {
                nodes.push(new Split(ns.node.left, !ns.side));
            }

            if (ns.node.right != null) {
                nodes.push(new Split(ns.node.right, !ns.side));
            }
        }
    }

    public Iterable<Point2D> range(RectHV rect) { // all points that are inside the rectangle
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        Stack<Point2D> inside = new Stack<>();
        if (root != null) {
            range(root, rect, inside);
        }
        return inside;
    }

    private void range(Node node, RectHV rect, Stack<Point2D> in) {
        if (rect.contains(node.point)) {
            in.push(node.point);
        }
        if (node.left != null && node.left.rect.intersects(rect)) {
            range(node.left, rect, in);
        }
        if (node.right != null && node.right.rect.intersects(rect)) {
            range(node.right, rect, in);
        }
    }

    public Point2D nearest(Point2D point) {
        if (point == null) {
            throw new IllegalArgumentException();
        }
        return nearest(root, point, Double.POSITIVE_INFINITY);
    }

    // Find the nearest point that is closer than distance
    private Point2D nearest(Node x, Point2D point, double distance) {
        if (x == null) {
            return null;
        }

        if (x.rect.distanceTo(point) >= distance) {
            return null;
        }

        Point2D nearestPoint = null;
        double nearestDistance = distance;
        double d;

        d = point.distanceTo(x.point);
        if (d < nearestDistance) {
            nearestPoint = x.point;
            nearestDistance = d;
        }

        // Choose subtree that is closer to point.

        Node firstNode = x.left;
        Node secondNode = x.right;

        if (firstNode != null && secondNode != null) {
            if (firstNode.rect.distanceTo(point) > secondNode.rect.distanceTo(point)) {
                firstNode = x.right;
                secondNode = x.left;
            }
        }

        Point2D firstNearestPoint = nearest(firstNode, point, nearestDistance);
        if (firstNearestPoint != null) {
            d = point.distanceTo(firstNearestPoint);
            if (d < nearestDistance) {
                nearestPoint = firstNearestPoint;
                nearestDistance = d;
            }
        }

        Point2D secondNearestPoint = nearest(secondNode, point, nearestDistance);
        if (secondNearestPoint != null) {
            d = point.distanceTo(secondNearestPoint);
            if (d < nearestDistance) {
                nearestPoint = secondNearestPoint;
                nearestDistance = d;
            }
        }

        return nearestPoint;
    }
}
