package controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Error controller
 */
@Controller
public class ErrorsController {

    @RequestMapping(value = "/400", method = RequestMethod.GET)
    public String error400() {
        return "400";
    }

    @RequestMapping(value = "/403", method = RequestMethod.GET)
    public String error403() {
        return "403";
    }

    @RequestMapping(value = "/404", method = RequestMethod.GET)
    public String error404() {
        return "404";
    }

    @RequestMapping(value = "/500", method = RequestMethod.GET)
    public String error500() {
        return "500";
    }
}
