package com.dukeKart.android.common;

public class AppsStrings {
    public static String apiUrl;
    public static String apiJson;
    public static String apiErrorCode;
    public static String apiErrorBody;
    public static String apiErrorDetail;
    public static String apiErrorException;
    public static String apiResponse;
    public static String defResponse;
    public static final String BASEJSON = "dukeKart";
    public static void setApiString(String apiString){
        apiUrl=apiString+"_Url";
        apiJson=apiString+"_Json";
        apiErrorCode=apiString+"_ErrorCode";
        apiErrorBody=apiString+"_ErrorBody";
        apiErrorDetail=apiString+"_ErrorDetail";
        apiErrorException=apiString+"_ErrorException";
        apiResponse=apiString+"_Response";
        defResponse="An error has encountered. Contact your service provider!";
    }
}
