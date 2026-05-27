package com.paic.nbm.analyticsservice.ExternalRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ExternalApi {

    public enum TypeRequest {
        IMSI, MSISDN
    }
    RestTemplate restTemplate;

    @Value("${external.api.endpoint}")
    String externalApiEndpoint;

    @Value("${external.api.timeout.connection}")
    int connectionTimeout;

    @Value("${external.api.timeout.read}")
    int readTimeout;

    public ExternalApi() {
    }

    @PostConstruct
    void initRestTemplate() {
        restTemplate = new RestTemplate(getClientHttpRequestFactory());
    }

    public List<String> getRequest(TypeRequest typeRequest, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HashMap<String, String> params = new HashMap<>();
        List<String> responseList = new ArrayList<>();
        String requestBody = "";
        switch (typeRequest) {
            case IMSI:
                params.put("${msisdn}", param);
                requestBody = getRequestBody("imsi-request.xml", params);
                break;

            case MSISDN:
                params.put("${imsi}", param);
                requestBody = getRequestBody("msisdn-request.xml", params);
                break;
        }
        if (!requestBody.isEmpty()) {
            try {
                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                        externalApiEndpoint,
                        HttpMethod.POST,
                        request,
                        String.class
                );
                responseList.addAll(convertResponse(response.getBody(), typeRequest));
            } catch (Exception ex) {
                log.error("Error on do the request -> " + ex.fillInStackTrace());
            }
        }
        return responseList;
    }

    private List<String> convertResponse(String response, TypeRequest typeRequest) throws JAXBException {
        JAXBContext jaxbContext;
        Unmarshaller unmarshaller;
        List<String> responseList = new ArrayList<>();
        switch (typeRequest) {
            case IMSI:
                jaxbContext = JAXBContext.newInstance(ImsiResponse.class);
                unmarshaller = jaxbContext.createUnmarshaller();
                ImsiResponse imsiResponse = (ImsiResponse) unmarshaller.unmarshal(new StringReader(response));
                log.debug("imsiResponse: " + imsiResponse.toString());
                if (imsiResponse.getImsiList() != null)
                    responseList.addAll(imsiResponse.getImsiList());
                break;

            case MSISDN:
                jaxbContext = JAXBContext.newInstance(MsisdnResponse.class);
                unmarshaller = jaxbContext.createUnmarshaller();
                MsisdnResponse msisdnResponse = (MsisdnResponse) unmarshaller.unmarshal(new StringReader(response));
                log.debug("msisdnResponse: " + msisdnResponse.toString());
                if (msisdnResponse.getMsisdnList() != null)
                    responseList.addAll(msisdnResponse.getMsisdnList());
                break;
        }
        return responseList;
    }

    private String getRequestBody(String file, HashMap<String, String> params) {
        String body;
        ClassPathResource xmlResource = new ClassPathResource("request/" + file);
        try {
            body = StreamUtils.copyToString(Objects.requireNonNull(xmlResource.getInputStream()), StandardCharsets.UTF_8);
            for (String key: params.keySet()) {
                body = body.replace(key, params.get(key));
            }
        } catch (Exception ex) {
            log.error("Error on get xml file -> " + file);
            body = "";
        }
        return body;
    }

    private SimpleClientHttpRequestFactory getClientHttpRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
