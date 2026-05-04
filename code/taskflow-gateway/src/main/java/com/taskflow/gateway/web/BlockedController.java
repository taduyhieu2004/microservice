package com.taskflow.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BlockedController {

    @RequestMapping("/_blocked")
    public ResponseEntity<Map<String, Object>> blocked() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", 404,
                "message", "Not Found",
                "data", Map.of("code", "endpoint_not_exposed", "detail", "Internal endpoints are not exposed via Gateway")
        ));
    }
}
