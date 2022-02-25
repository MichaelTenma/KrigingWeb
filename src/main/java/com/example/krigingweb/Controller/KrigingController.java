package com.example.krigingweb.Controller;

import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/kriging")
@ResponseBody
public class KrigingController {
    private final SamplePointService samplePointService;
//    private final Interpolater interpolater;
    private final LandService landService;

    @Autowired
    public KrigingController(SamplePointService samplePointService, LandService landService) {
        this.samplePointService = samplePointService;
//        this.interpolater = interpolater;
        this.landService = landService;
    }

    @GetMapping("generate")
    public String generate() throws ParseException {

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(1000000), 3857);

        WKTReader wktReader = new WKTReader(geometryFactory);
        Polygon polygon = (Polygon) wktReader.read("POLYGON((12591648.430045 2596006.9050781,12591839.5226157 2595619.94262241,12592035.3925007 2595591.2787368,12593411.2590098 2595046.66491027,12593497.2506666 2595199.53896684,12593545.0238093 2595266.42136659,12593511.5826094 2595533.95096559,12593478.1414096 2595648.60650801,12593072.0696968 2595892.24953567,12592895.3090689 2595973.46387823,12592751.9896409 2595997.35044957,12592580.0063272 2595987.79582103,12592408.0230136 2595968.68656396,12592322.0313568 2595968.68656396,12592130.938786 2595968.68656396,12591997.1739865 2595944.79999262,12591901.6277012 2595949.57730689,12591786.9721588 2595983.01850676,12591739.1990161 2596011.68239237,12591681.8712449 2596030.79164944,12591648.430045 2596006.9050781))");

        Envelope envelope = polygon.getEnvelopeInternal();

        StringBuilder result = new StringBuilder();
        double cellSize = 30;
        double halfCellSize = cellSize / 2;
        for(double beginY = envelope.getMinY(); beginY < envelope.getMaxY(); beginY += cellSize){
            for(double beginX = envelope.getMinX(); beginX < envelope.getMaxX(); beginX += cellSize){
                Coordinate coordinate = new Coordinate(beginX + halfCellSize, beginY + halfCellSize);
                Point point = geometryFactory.createPoint(coordinate);
                boolean isContain = polygon.isWithinDistance(point, halfCellSize);
                if(isContain){
                    result.append(String.format(
                        "UNION SELECT ST_Transform(ST_GeometryFromText('%s', 3857), 4326)\n", point.toText()
                    ));
                }
            }
        }

        return result.toString();
    }

//    @GetMapping("/train")
//    public String train(){
//        RegressionDataSet[] regressionDataSetArray = SamplePointService.samplePointToRegressionDataSet(
//                samplePointService.list(), SoilNutrientEnum.N
//        );
//        RegressionDataSet trainRegressionDataSet = regressionDataSetArray[0];
//        RegressionDataSet testRegressionDataSet = regressionDataSetArray[1];
//        Regressor regressor = this.interpolater.trainOrdinaryKriging(trainRegressionDataSet).first;
//
//
//        List<DataPointPair<Double>> trainList = trainRegressionDataSet.getDPPList();
//        ErrorEntity trainErrorEntity = new ErrorEntity(
//                ErrorEntity.calError(trainList, regressor)
//        );
//
//        List<DataPointPair<Double>> testList = testRegressionDataSet.getDPPList();
//        ErrorEntity testErrorEntity = new ErrorEntity(
//                ErrorEntity.calError(testList, regressor)
//        );
//
//        return String.format(
//            "train: <br/>%s, size: %d<br/>test: <br/>%s, size: %d",
//            trainErrorEntity, trainList.size(),
//            testErrorEntity, testList.size()
//        );
//    }
//
//    @GetMapping("/interpolate")
//    public List<LandEntity> interpolate(){
//        return this.interpolater.interpolate(
//            this.samplePointService.list(), this.landService.list(), 300
//        );
//    }
}
