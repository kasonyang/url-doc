package site.kason.urldoc.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author KasonYang
 */
@RestController
@RequestMapping("example")
public class ExampleController {

    /**
     * hello world request
     * @return
     */
    @RequestMapping("hello")
    public String hello() {
        return "hello,world";
    }

}
