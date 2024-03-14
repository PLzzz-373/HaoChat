package com.gugugu.haochat.common.exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException{
    protected Integer errorCode;
    protected String errorMsg;
    public BusinessException(String errorMsg){
        super(errorMsg);
        this.errorCode = BusinessErrorEnum.BUSINESS_ERROR.getErrorCode();
        this.errorMsg = errorMsg;
    }
    public BusinessException(Integer errorCode, String errorMsg){
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
}
