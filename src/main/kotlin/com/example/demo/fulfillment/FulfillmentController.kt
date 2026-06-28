package com.example.demo.fulfillment

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/shipments")
class FulfillmentController(private val fulfillmentService: FulfillmentService) {

    @PostMapping
    fun createShipment(@RequestBody request: ShipmentRequest): ResponseEntity<String> {
        val shipmentId = fulfillmentService.createShipment(request)
        return ResponseEntity.ok(shipmentId)
    }

    @PutMapping("/{shipmentId}/status")
    fun changeStatus(
        @PathVariable shipmentId: String,
        @RequestBody request: ShipmentStatusRequest
    ): ResponseEntity<Void> {
        fulfillmentService.changeStatus(shipmentId, request)
        return ResponseEntity.noContent().build()
    }
}
