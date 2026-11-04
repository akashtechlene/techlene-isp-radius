package com.radiusengine;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {

        String attrval="{mtk_download_speed}/{mtk_download_speed}";
        String arr[]=attrval.split("\"");
        System.out.println("arr size :"+arr.length);
       for (String segment:arr){
           System.out.println("segment :"+segment);
       }

    }
}