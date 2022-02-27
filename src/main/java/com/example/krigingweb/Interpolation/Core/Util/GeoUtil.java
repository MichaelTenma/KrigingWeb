package com.example.krigingweb.Interpolation.Core.Util;

import jsat.classifiers.DataPoint;
import jsat.linear.DenseVector;
import jsat.linear.Vec;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

public class GeoUtil {
    public static final int srid = 3857;
    public static final PrecisionModel precisionModel = new PrecisionModel(1000000);
    public static final GeometryFactory geometryFactory = new GeometryFactory(precisionModel, srid);
    public static final WKTReader wktReader = new WKTReader(geometryFactory);

    public static final double samplePointMaxDistance = 5000;

    public static DataPoint buildDataPoint(Point point){
        return new DataPoint(new DenseVector(new double[]{point.getX(), point.getY()}));
    }
}