package com.nchain.headerSV.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
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
