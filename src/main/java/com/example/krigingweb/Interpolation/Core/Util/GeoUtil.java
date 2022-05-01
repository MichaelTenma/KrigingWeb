package com.example.krigingweb.Interpolation.Core.Util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

public class GeoUtil {
    public static final int srid = 3857;
    public static final PrecisionModel precisionModel = new PrecisionModel(1000000);
    public static final GeometryFactory geometryFactory = new GeometryFactory(precisionModel, srid);
    public static final WKTReader wktReader = new WKTReader(geometryFactory);
    private static final ThreadLocal<WKBReader> wkbReaderThreadLocal
            = ThreadLocal.withInitial(() -> new WKBReader(geometryFactory));

    private static final ThreadLocal<WKBWriter> wkbWriterThreadLocal
            = ThreadLocal.withInitial(WKBWriter::new);


    public static final double samplePointMaxDistance = 5000;

    public static <T extends Geometry> T toGeometry(byte[] bytes){
        T geom = null;
        WKBReader wkbReader = wkbReaderThreadLocal.get();
        if(wkbReader != null){
            try {
                geom = (T)wkbReader.read(bytes);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return geom;
    }

    public static <T extends Geometry> byte[] toBytes(T geom){
        byte[] bytes = null;
        WKBWriter wkbWriter = wkbWriterThreadLocal.get();
        if(wkbWriter != null){
            bytes = wkbWriter.write(geom);
        }
        return bytes;
    }
}