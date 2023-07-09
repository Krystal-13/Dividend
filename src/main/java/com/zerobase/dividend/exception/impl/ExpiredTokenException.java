package com.zerobase.dividend.exception.impl;

import com.zerobase.dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ExpiredTokenException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.REQUEST_TIMEOUT.value();
    }

    @Override
    public String getMessage() {
        return "인증 토큰이 만료되었습니다.";
    }
}
