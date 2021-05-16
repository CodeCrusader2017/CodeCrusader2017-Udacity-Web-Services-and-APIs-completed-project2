package com.udacity.pricingservice.api;

import java.net.URI;
import java.util.List;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("pricing-service")
public class PricingController {
    //private WebClient client;

    //@Autowired
    //private RestTemplate restTemplate;

    @Autowired
    DiscoveryClient discoveryClient;
    @RequestMapping("currency")
    public String getMicroserviceName()
    {
        List<ServiceInstance> list = discoveryClient.getInstances("pricing-service");
        ServiceInstance service2 = list.get(0);
        URI micro2URI = service2.getUri();

        return micro2URI.toString();
    }
}

