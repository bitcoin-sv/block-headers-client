package com.nchain.headerSV.service.geolocation;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.nchain.bna.network.PeerAddress;
import com.nchain.bna.tools.files.FileUtils;
import com.nchain.headerSV.domain.PeerLocationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2019 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 2019-10-10 15:46
 */
@Service
public class GeolocationServiceImpl implements GeolocationService {

    private static final String DB_FILENAME = "GeoLite2-City.mmdb";

    private DatabaseReader dbReader;

    @Autowired
    FileUtils fileUtils;

    @Override
    public Optional<PeerLocationInfo> geoLocate(PeerAddress peerAddress)  {
        try {
            InetAddress ipAddress = InetAddress.getByName(peerAddress.getIp().getHostName());
            CityResponse response = dbReader.city(ipAddress);

            String countryName = response.getCountry().getName();
            String cityName = response.getCity().getName();
            String postal = response.getPostal().getCode();
            PeerLocationInfo peerLocationInfo = new PeerLocationInfo(cityName, countryName, postal);
            return Optional.of(peerLocationInfo);
        } catch (GeoIp2Exception | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @PostConstruct
    public void  databaseReaderinit() throws IOException {
        String filePath = Paths.get(fileUtils.getDataFolder().toString(), DB_FILENAME).toString();
        InputStream inputStream = new FileInputStream(filePath);
        dbReader =  new DatabaseReader.Builder(inputStream).build();
    }
}
