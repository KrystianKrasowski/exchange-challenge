package org.kkrasowski.exchange.application.resources

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class RootResource {

    @RequestMapping(method = [RequestMethod.GET])
    fun sayHello(): String {
        return "Hello"
    }
}
