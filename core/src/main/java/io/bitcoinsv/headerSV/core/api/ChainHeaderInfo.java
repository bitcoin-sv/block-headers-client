package io.bitcoinsv.headerSV.core.api;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import java.math.BigInteger;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 *
 * This class contains info about one Block Header and info about its position in the blockchain.
 * IT represetns the "State" of a Block in the chain.
 */
public class ChainHeaderInfo {
    private HeaderReadOnly header;
    private String state;
    private BigInteger chainWork;
    private Integer height;
    private Integer confirmations;

    /** Constructor */
    public ChainHeaderInfo() {}

    /** Constructor */
    public ChainHeaderInfo(HeaderReadOnly header,
                           String state,
                           BigInteger chainWork,
                           Integer height,
                           Integer confirmations) {
        this.header = header;
        this.state = state;
        this.chainWork = chainWork;
        this.height = height;
        this.confirmations = confirmations;
    }

    public HeaderReadOnly getHeader()   { return this.header;}
    public String getState()            { return this.state;}
    public BigInteger getChainWork()    { return this.chainWork;}
    public Integer getHeight()          { return this.height;}
    public Integer getConfirmations()   { return this.confirmations;}

    public static ChainHeaderInfoBuilder builder() {
        return new ChainHeaderInfoBuilder();
    }

    /**
     * Builder
     */
    public static class ChainHeaderInfoBuilder {
        private HeaderReadOnly header;
        private String state;
        private BigInteger chainWork;
        private Integer height;
        private Integer confirmations;

        public ChainHeaderInfoBuilder header(HeaderReadOnly header) {
            this.header = header;
            return this;
        }

        public ChainHeaderInfoBuilder state(String state) {
            this.state = state;
            return this;
        }

        public ChainHeaderInfoBuilder chainWork(BigInteger chainWork) {
            this.chainWork = chainWork;
            return this;
        }

        public ChainHeaderInfoBuilder height(Integer height) {
            this.height = height;
            return this;
        }

        public ChainHeaderInfoBuilder confirmations(Integer confirmations) {
            this.confirmations = confirmations;
            return this;
        }

        public ChainHeaderInfo build() {
            return new ChainHeaderInfo(
                    this.header,
                    this.state,
                    this.chainWork,
                    this.height,
                    this.confirmations
            );
        }
    }
}