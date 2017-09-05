import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.lang.*;
import java.util.Comparator;
import java.util.Stack;

public class KdTree {
    private Node root;
    private int size;

    public KdTree() {
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void insert(Point2D point) {
        if (point == null) {
            throw new NullPointerException();
        }
        root = insert(root, point, true, null, 0);
    }

    public boolean contains(Point2D point) {
        if (point == null) {
            throw new NullPointerException();
        }
        return contain(root, point, true);
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
            throw new java.lang.NullPointerException();
        }
        Stack<Point2D> inside = new Stack<>();
        if (root != null) {
            range(root, rect, inside);
        }
        return inside;
    }

    public Point2D nearest(Point2D point) {
        if (point == null) {
            throw new NullPointerException();
        }
        if (root == null) {
            return null;
        }
        return FindNear(root, point, root.point);
    }

    private Point2D FindNear(Node node, Point2D actual, Point2D nearest) {
        if (node == null) {
            return nearest;
        }

        double d2n = actual.distanceSquaredTo(nearest);
        double d2r = node.rect.distanceSquaredTo(actual);

        // Is the nearest point closer than this node's rectangle? If so, this
        // node can't contain anything nearer, so return
        if (d2n < d2r) {
            return nearest;
        }

        double d2p = actual.distanceSquaredTo(node.point);

        // Is this node nearer than the current nearest? If so, update
        if (d2p < d2n) {
            nearest = node.point;
        }

        // No children? Return current nearest
        if (node.left == null && node.right == null) {
            return nearest;
        }

        // Only left/bottom child? Recurse to it
        if (node.right == null) {
            return FindNear(node.left, actual, nearest);
        }

        // Only right/top child? Recurse to it
        if (node.left == null) {
            return FindNear(node.right, actual, nearest);
        }

        // Try both children, the one containing the query point first
        if (node.left.rect.contains(actual)) {
            nearest = FindNear(node.left, actual, nearest);
            nearest = FindNear(node.right, actual, nearest);
        } else {
            nearest = FindNear(node.right, actual, nearest);
            nearest = FindNear(node.left, actual, nearest);
        }

        return nearest;
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

    private boolean contain(Node node, Point2D point, boolean side) {
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

        if (comp > 0) {
            return contain(node.right, point, !side);
        } else {
            return contain(node.left, point, !side);
        }
    }


    private Node insert(Node node, Point2D point, boolean side, Node parent, int x) {
        if (node == null) {
            RectHV rect;
            if (parent == null) {
                rect = new RectHV(0.0, 0.0, 1.0, 1.0);
            } else {
                if (!side) {
                    if (x > 0) {
                        rect = new RectHV(parent.point.x(), parent.rect.ymin(), parent.rect.xmax(), parent.rect.ymax());
                    } else {
                        rect = new RectHV(parent.rect.xmin(), parent.rect.ymin(), parent.point.x(), parent.rect.ymax());
                    }
                } else {
                    if (x > 0) {
                        rect = new RectHV(parent.rect.xmin(), parent.point.y(), parent.rect.xmax(), parent.rect.ymax());
                    } else {
                        rect = new RectHV(parent.rect.xmin(), parent.rect.ymin(), parent.rect.xmax(), parent.point.y());
                    }
                }
            }
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

    private static class Split {
        private Node node;
        private boolean side;

        private Split(Node node, Boolean side) {
            this.node = node;
            this.side = side;
        }
    }

    private static class Node {
        private Point2D point;
        private RectHV rect;
        private Node left;
        private Node right;

        private Node(Point2D point, RectHV rect) {
            this.point = point;
            this.rect = rect;
        }
    }

}
