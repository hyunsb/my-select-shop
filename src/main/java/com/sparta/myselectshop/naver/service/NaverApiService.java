package com.sparta.myselectshop.naver.service;

import com.sparta.myselectshop.naver.dto.ItemDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "NAVER API")
@Service
public class NaverApiService {

    private static final String NAVER_API_URI = "https://openapi.naver.com";
    private static final String NAVER_API_SEARCH_PATH = "/v1/search/shop.json";
    private static final String NAVER_SEARCH_PARAM_DISPLAY = "display";
    private static final int NAVER_SEARCH_PARAM_DISPLAY_VALUE = 15;
    private static final String NAVER_SEARCH_PARAM_QUERY = "query";

    private static final String NAVER_API_HEADER_CLIENT_ID = "X-Naver-Client-Id";
    private static final String NAVER_API_HEADER_SECRET = "X-Naver-Client-Secret";

    private final RestTemplate restTemplate;

    private final String clientId;
    private final String secretKey;

    public NaverApiService(
        RestTemplateBuilder builder,
        @Value("${naver.api.client-id}") String clientId,
        @Value("${naver.api.secretKey}") String secretKey) {

        this.restTemplate = builder.build();
        this.clientId = clientId;
        this.secretKey = secretKey;
    }

    public List<ItemDto> searchItems(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
            .fromUriString(NAVER_API_URI)
            .path(NAVER_API_SEARCH_PATH)
            .queryParam(NAVER_SEARCH_PARAM_DISPLAY, NAVER_SEARCH_PARAM_DISPLAY_VALUE)
            .queryParam(NAVER_SEARCH_PARAM_QUERY, query)
            .encode()
            .build()
            .toUri();
        log.info("uri = " + uri);

        RequestEntity<Void> requestEntity = RequestEntity
            .get(uri)
            .header(NAVER_API_HEADER_CLIENT_ID, clientId)
            .header(NAVER_API_HEADER_SECRET, secretKey)
            .build();

        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        log.info("NAVER API Status Code : " + responseEntity.getStatusCode());

        return fromJSONtoItems(responseEntity.getBody());
    }

    public List<ItemDto> fromJSONtoItems(String responseEntity) {
        JSONObject jsonObject = new JSONObject(responseEntity);
        JSONArray items  = jsonObject.getJSONArray("items");
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (Object item : items) {
            ItemDto itemDto = new ItemDto((JSONObject) item);
            itemDtoList.add(itemDto);
        }

        return itemDtoList;
    }
}
