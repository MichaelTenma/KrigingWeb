package com.example.krigingweb.Interpolation.Core.Enum;

public enum InterpolatedStatusEnum {
    UnStart("UnSt"), /* 未开始插值 */
    Prepare("Prep"), /* 正在准备开始插值 */
    Current("Curr"), /* 正在插值 */
    Done("Done"), /* 已完成插值 */
    ;

    private String value;
    InterpolatedStatusEnum(String value){
        this.value = value;
    }

    @Override
    public String toString(){
        return this.value;
    }
}
