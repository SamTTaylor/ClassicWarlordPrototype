package samueltaylor.classicwarlordprototype.poly2tri.transform.coordinate;

import java.util.List;

import samueltaylor.classicwarlordprototype.poly2tri.geometry.primitives.Point;

public abstract interface CoordinateTransform
{
    public abstract void transform(Point p, Point store);
    public abstract void transform(Point p);
    public abstract void transform(List<? extends Point> list);
}
