package io.bitcoinsv.headerSV.app.config;

import io.bitcoinsv.headerSV.rest.v1.config.HeaderSvRestConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 31/12/2021
 */
@Configuration
@Import(HeaderSvRestConfig.class)
@ComponentScan("io.bitcoinsv.headerSV.rest.v1.controller")
public class HeaderSVConfig {
}
