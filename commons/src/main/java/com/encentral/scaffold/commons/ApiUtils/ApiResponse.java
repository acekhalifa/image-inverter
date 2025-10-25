
package com.encentral.scaffold.commons.ApiUtils;

import java.util.Map;

public class ApiResponse {
    public boolean success;
    public String message;
    public Object data;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
