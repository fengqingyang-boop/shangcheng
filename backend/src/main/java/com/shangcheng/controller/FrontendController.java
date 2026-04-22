package com.shangcheng.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FrontendController {
    
    @GetMapping("/")
    public Map<String, String> index() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "商城系统 API 服务已启动");
        result.put("frontend", "请访问 http://localhost:3000");
        return result;
    }
}
