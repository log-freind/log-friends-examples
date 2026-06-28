package com.example.demo.fulfillment

import com.logfriends.agent.annotation.LogField

data class ShipmentStatusRequest(
    @field:LogField(description = "New shipment status such as IN_TRANSIT or DELIVERED", type = "STRING")
    val shipmentStatus: String,

    @field:LogField(description = "Warehouse or hub code where the status was updated", type = "STRING")
    val warehouseCode: String,

    @field:LogField(description = "Shipping carrier reporting the status", type = "STRING")
    val carrier: String
)
