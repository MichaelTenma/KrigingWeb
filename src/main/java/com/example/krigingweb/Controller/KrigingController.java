package com.example.krigingweb.Controller;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Enum.SoilNutrientEnum;
import com.example.krigingweb.Interpolation.Kriging.Variogram.SphericalVariogram;
import com.example.krigingweb.Math.MathUtil;
import com.example.krigingweb.Service.InterpolationService;
import com.example.krigingweb.Service.LandService;
import com.example.krigingweb.Service.SamplePointService;
import com.example.krigingweb.Util.Tuple;
import jsat.classifiers.DataPointPair;
import jsat.regression.OrdinaryKriging;
import jsat.regression.RegressionDataSet;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/kriging")
@ResponseBody
public class KrigingController {
    private final SamplePointService samplePointService;
    private final InterpolationService interpolationService;
    private final LandService landService;

    @Autowired
    public KrigingController(SamplePointService samplePointService, InterpolationService interpolationService, LandService landService) {
        this.samplePointService = samplePointService;
        this.interpolationService = interpolationService;
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

    private OrdinaryKriging ordinaryKriging = null;
    private RegressionDataSet testRegressionDataSet = null;
    @GetMapping("/train")
    public String train(){
        RegressionDataSet[] regressionDataSetArray = SamplePointService.samplePointToRegressionDataSet(
                samplePointService.list(), SoilNutrientEnum.N
        );
        RegressionDataSet trainRegressionDataSet = regressionDataSetArray[0];
        this.testRegressionDataSet = regressionDataSetArray[1];
        this.ordinaryKriging = this.interpolationService.trainOrdinaryKriging(trainRegressionDataSet).first;

        System.out.println(this.calRMSE(trainRegressionDataSet.getDPPList()));
        return "success";
    }

    @GetMapping("/regress")
    public String regress(){
        List<DataPointPair<Double>> list = this.testRegressionDataSet.getDPPList();
        return this.calRMSE(list);
    }

    @GetMapping("/interpolate")
    public List<LandEntity> interpolate(){
        List<LandEntity> landEntityList = this.interpolationService.interpolate(
            this.samplePointService.list(), this.landService.list(), 300
        );
        return landEntityList;
    }

    private String calRMSE(List<DataPointPair<Double>> list){
        List<Double> errorList = new ArrayList<>(list.size());

        for(DataPointPair<Double> dataPointPair : list){
            double value = dataPointPair.getPair();
            double predictValue = ordinaryKriging.regress(dataPointPair.getDataPoint());
            errorList.add(predictValue - value);
        }
        return String.format("MAE: %f, RMSE: %f, size: %d", MathUtil.MAE(errorList), MathUtil.RMSE(errorList), errorList.size());
        // 12761016.3187, 2637209.9114
    }
}
