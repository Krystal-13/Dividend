package com.zerobase.dividend.exception.impl;

import com.zerobase.dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NoUserException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.NOT_FOUND.value();
    }

    @Override
    public String getMessage() {
        return "사용자가 존재하지 않습니다.";
    }
}
