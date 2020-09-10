package com.nchain.headerSV.service.geolocation;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.nchain.headerSV.domain.PeerLocationInfo;
import com.nchain.jcl.base.tools.files.FileUtils;
import com.nchain.jcl.base.tools.files.FileUtilsBuilder;
import com.nchain.jcl.net.network.PeerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2019 Bitcoin Association
  * Copyright (c) 2018-2020 nChain Ltd
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
        //String filePath = Paths.get(fileUtils.getRootPath().toString(), DB_FILENAME).toString();
        //    InputStream inputStream = new FileUtilsBuilder().copyFromclasspatch().build(this.getClass().getClassLoader()))
        FileUtils fileUtils = new FileUtilsBuilder().copyFromClasspath().build(this.getClass().getClassLoader());
        String filePath = Paths.get(fileUtils.getRootPath().toString(), DB_FILENAME).toString();

        dbReader =  new DatabaseReader.Builder(new File(filePath)).build();
    }
}
