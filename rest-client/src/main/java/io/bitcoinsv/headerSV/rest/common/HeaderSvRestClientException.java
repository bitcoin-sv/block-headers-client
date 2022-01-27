package io.bitcoinsv.headerSV.rest.common;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 31/12/2021
 */
public class HeaderSvRestClientException extends RuntimeException {
    public HeaderSvRestClientException(String message) {
        super(message);
    }

    public HeaderSvRestClientException(Exception ex){
        super(ex);
    }
}
