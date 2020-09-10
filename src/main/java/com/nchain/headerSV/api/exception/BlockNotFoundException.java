package com.nchain.headerSV.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 13/08/2020
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class BlockNotFoundException extends RuntimeException  {
    public BlockNotFoundException() {
        super();
    }
    public BlockNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public BlockNotFoundException(String message) {
        super(message);
    }
    public BlockNotFoundException(Throwable cause) {
        super(cause);
    }
}
