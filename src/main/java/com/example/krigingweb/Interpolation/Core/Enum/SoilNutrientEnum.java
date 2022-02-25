package com.example.krigingweb.Interpolation.Core.Enum;

public enum SoilNutrientEnum {
    N("碱解氮", 0, 250),
    P("有效磷", 0, 80),
    K("速效钾", 0, 300),
    OC("有机质", 0, 80),
    PH("pH", 0, 14);

    public final String name;
    public final double leftRange;
    public final double rightRange;

    SoilNutrientEnum(String name, double leftRange, double rightRange) {
        this.name = name;
        this.leftRange = leftRange;
        this.rightRange = rightRange;
    }
}
