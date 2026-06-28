package com.example.demo.shop

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ShopPageController {
    @GetMapping("/")
    fun index(): String = "forward:/shop.html"

    @GetMapping("/shop")
    fun shop(): String = "forward:/shop.html"
}
