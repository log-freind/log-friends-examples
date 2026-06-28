package com.example.demo.catalog

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class CatalogController(private val catalogService: CatalogService) {

    @GetMapping
    fun listProducts(
        @RequestParam(name = "q", required = false) query: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) stockStatus: String?,
        @RequestParam(required = false) minPrice: Int?,
        @RequestParam(required = false) maxPrice: Int?
    ): ResponseEntity<List<ProductResponse>> {
        return ResponseEntity.ok(catalogService.listProducts(query, category, stockStatus, minPrice, maxPrice))
    }

    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: String): ResponseEntity<ProductResponse> {
        val product = catalogService.getProduct(productId)
        return if (product == null) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.ok(product)
        }
    }
}
