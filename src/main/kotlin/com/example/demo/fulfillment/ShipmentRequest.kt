package com.example.demo.fulfillment

import com.logfriends.agent.annotation.LogField

data class ShipmentRequest(
    @field:LogField(description = "Order identifier connected to this shipment", type = "STRING")
    val orderId: String,

    @field:LogField(description = "Shipping carrier such as CJ_LOGISTICS or UPS", type = "STRING")
    val carrier: String,

    @field:LogField(description = "Carrier tracking number visible to the customer", type = "STRING")
    val trackingNumber: String,

    @field:LogField(description = "Current shipment status such as READY_TO_SHIP", type = "STRING")
    val shipmentStatus: String,

    @field:LogField(description = "Fulfillment warehouse code that packs the order", type = "STRING")
    val warehouseCode: String
)
