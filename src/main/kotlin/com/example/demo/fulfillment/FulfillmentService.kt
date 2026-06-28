package com.example.demo.fulfillment

import com.logfriends.agent.annotation.LogEvent
import com.logfriends.agent.annotation.LogField
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FulfillmentService {
    private val log = LoggerFactory.getLogger(FulfillmentService::class.java)

    @LogEvent(
        name = "shipmentCreated",
        description = "Shipment creation business eventName",
        apiMethod = "POST",
        apiPath = "/shipments",
        apiDescription = "Creates a shipment for a paid shopping mall order"
    )
    fun createShipment(
        @LogField(description = "Shipment creation request DTO", type = "JSON")
        request: ShipmentRequest
    ): String {
        log.info(
            "Creating shipment for order {} with carrier {}, tracking {}, warehouse {}",
            request.orderId,
            request.carrier,
            request.trackingNumber,
            request.warehouseCode
        )
        return "SHP-" + System.currentTimeMillis()
    }

    @LogEvent(
        name = "shipmentStatusChanged",
        description = "Shipment status change business eventName",
        apiMethod = "PUT",
        apiPath = "/shipments/{shipmentId}/status",
        apiDescription = "Changes the current shipment status"
    )
    fun changeStatus(
        @LogField(description = "Shipment identifier whose status changed", type = "STRING")
        shipmentId: String,
        @LogField(description = "Shipment status change request DTO", type = "JSON")
        request: ShipmentStatusRequest
    ) {
        log.info(
            "Changing shipment {} status to {} at warehouse {}",
            shipmentId,
            request.shipmentStatus,
            request.warehouseCode
        )
    }
}
