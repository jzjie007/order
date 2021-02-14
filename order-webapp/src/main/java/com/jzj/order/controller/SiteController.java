package com.jzj.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@ResponseBody
@RestController
@RequestMapping("temp")
public class SiteController {
    private static Logger log = LoggerFactory.getLogger(SiteController.class);


    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("ping")
    public Object ping() {
        log.info("进入ping");
        return "pong order";
    }


    @RequestMapping("log")
    public Object log() {
        log.info("this is info log");
        log.error("this is error log");
        log.debug("this is debug log");
        log.warn("this is warn log");
        log.trace("this is trace log");
        return "123";
    }

    @RequestMapping("http")
    public Object httpQuery() {

        String roomUrl = "http://localhost:9988/temp/ping";
        URI ping = URI.create(roomUrl);
        String pingResult = restTemplate.getForObject(ping, String.class);

        return pingResult;
    }

}