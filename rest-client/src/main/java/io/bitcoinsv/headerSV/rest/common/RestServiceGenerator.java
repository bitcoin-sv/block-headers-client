package io.bitcoinsv.headerSV.rest.common;

import com.google.gson.JsonSyntaxException;
import okhttp3.OkHttpClient;
import org.springframework.http.HttpStatus;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 19/08/2021
 */
public class RestServiceGenerator {

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .client(new OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit;

    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        retrofit = builder.baseUrl(baseUrl).build();
        return retrofit.create(serviceClass);
    }

    /**
     * Execute a REST call and blocks until the response is received.
     */
    public static <T> T executeSync(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else if (response.code() == HttpStatus.NOT_FOUND.value()) {
                return null;
            } else {
                throw new RuntimeException("HSV client returned an unexpected error");
            }
        } catch (JsonSyntaxException jsone) {
            // This error should not be triggered. If the server returns a 500 code, json deserialization should not take place..
            return null;
        } catch (Exception e) {
            throw new HeaderSvRestClientException(e.getMessage());
        }
    }
}