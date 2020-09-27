package com.inotee.swarmbuy.entity.result.my;

public abstract class BaseService {
    public BaseService() {
    }

    protected ResponseEntity buildSuccess(){
        return ResponseEntity.builder()
                .success(true)
                .resulteMessage(ErrorCodeEnum.RETURN_SUCCEE.getMsgKey())
                .build();
    }

    protected ResponseEntity buildError(){
        return ResponseEntity.builder()
                .success(false)
                .resulteMessage(ErrorCodeEnum.UNKNOWN_ERROR.getMsgKey())
                .build();
    }

    protected <T> ResponseEntity<T> buildSuccess(T data){
        return ResponseEntity.<T>builder()
                .success(true)
                .result(data)
                .resulteMessage(ErrorCodeEnum.RETURN_SUCCEE.getMsgKey())
                .build();
    }

    protected <T> ResponseEntity<T> buildError(ErrorCodeEnum errorCodeEnum){
        return ResponseEntity.<T>builder()
                .success(false)
                .resultCode(errorCodeEnum.getCode())
                .resulteMessage(errorCodeEnum.getMsgKey())
                .build();
    }
}
