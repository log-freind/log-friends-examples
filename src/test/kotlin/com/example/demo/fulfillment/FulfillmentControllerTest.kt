package com.example.demo.fulfillment

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FulfillmentController::class)
class FulfillmentControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean lateinit var fulfillmentService: FulfillmentService

    @Test
    fun `POST shipments returns shipment id`() {
        val request = ShipmentRequest(
            orderId = "ORD-1001",
            carrier = "CJ_LOGISTICS",
            trackingNumber = "CJ-924812341",
            shipmentStatus = "READY_TO_SHIP",
            warehouseCode = "WH-SEOUL-01"
        )
        given(fulfillmentService.createShipment(request)).willReturn("SHP-1001")

        mockMvc.perform(
            post("/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "orderId": "ORD-1001",
                      "carrier": "CJ_LOGISTICS",
                      "trackingNumber": "CJ-924812341",
                      "shipmentStatus": "READY_TO_SHIP",
                      "warehouseCode": "WH-SEOUL-01"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(content().string("SHP-1001"))
    }

    @Test
    fun `POST shipments returns 400 when required field is missing`() {
        mockMvc.perform(
            post("/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"orderId":"ORD-1001","carrier":"CJ_LOGISTICS"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `PUT shipment status returns 204`() {
        val request = ShipmentStatusRequest(
            shipmentStatus = "IN_TRANSIT",
            warehouseCode = "HUB-DAEJEON-01",
            carrier = "CJ_LOGISTICS"
        )
        willDoNothing().given(fulfillmentService).changeStatus("SHP-1001", request)

        mockMvc.perform(
            put("/shipments/SHP-1001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "shipmentStatus": "IN_TRANSIT",
                      "warehouseCode": "HUB-DAEJEON-01",
                      "carrier": "CJ_LOGISTICS"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isNoContent)
    }
}
