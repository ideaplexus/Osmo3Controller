package com.dji.ImportSDKDemo;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;

public class CommonUtils {

    public static CommonCallbacks.CompletionCallback GenerateCommonCallback(){
        return new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError != null) {
                    ToastUtils.setResultToToast(djiError.getDescription());
                }
            }
        };
    }

}
