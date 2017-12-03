package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableAutoConfiguration
public class ReceiverApplication implements EmbeddedServletContainerCustomizer {

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(8888);
    }

    public static void main(String[] args) {
		SpringApplication.run(ReceiverApplication.class, args);
	}

    @RequestMapping("/get")
    public String getGet(@RequestParam String name){
        System.out.println(name);
        return "get success!";
    }

    @RequestMapping(path = "/post", method = RequestMethod.POST)
    public String getPost(@RequestParam String score){
        System.out.println(score);
        return "Post!";
    }
}
