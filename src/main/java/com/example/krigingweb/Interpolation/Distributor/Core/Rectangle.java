package com.example.krigingweb.Interpolation.Distributor.Core;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

public class Rectangle {
    public final double left;
    public final double right;
    public final double top;
    public final double bottom;

    public Rectangle(double left, double right, double top, double bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public Polygon toPolygon(GeometryFactory geometryFactory){
        return geometryFactory.createPolygon(
            new Coordinate[]{
                new Coordinate(left, bottom),
                new Coordinate(right, bottom),
                new Coordinate(right, top),
                new Coordinate(left, top),
                new Coordinate(left, bottom)
            }
        );
    }

    public double getArea(){
        return (this.right - this.left) * (this.top - this.bottom);
    }

    public Rectangle buffer(double distance){
        return new Rectangle(
                this.left - distance,
                this.right + distance,
                this.top + distance,
                this.bottom - distance
        );
    }

    public Rectangle bufferFromCenter(double distance){
        double x_center = (this.right - this.left) / 2 + this.left;
        double y_center = (this.top - this.bottom) / 2 + this.bottom;
        return new Rectangle(
            x_center - distance,
            x_center + distance,
            y_center + distance,
            y_center - distance
        );
    }

    public Rectangle bufferFromBorder(double distance){
        double x_distance = (this.right - this.left) / 2;
        double y_distance = (this.top - this.bottom) / 2;
        double x_center = x_distance + this.left;
        double y_center = y_distance + this.bottom;

        return new Rectangle(
            x_center - distance - x_distance,
            x_center + distance + x_distance,
            y_center + distance + y_distance,
            y_center - distance - y_distance
        );
    }

    public double getWidth(){
        return this.right - this.left;
    }

    public double getHeight(){
        return this.top - this.bottom;
    }

    @Override
    public String toString() {
        return String.format(
            "Polygon((%f %f,%f %f,%f %f,%f %f,%f %f))",
            left, bottom, /* 左下角 */
            right, bottom, /* 右下角 */
            right, top, /* 右上角 */
            left, top, /* 左上角 */
            left, bottom/* 左下角 */
        );
    }
}