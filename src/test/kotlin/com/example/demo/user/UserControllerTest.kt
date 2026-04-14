package com.example.demo.user

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @MockitoBean  lateinit var userService: UserService

    @Test
    fun `POST users - 유효한 요청은 200과 userId 반환`() {
        given(userService.register("홍길동", "hong@example.com")).willReturn("USR-001")

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"홍길동","email":"hong@example.com"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("USR-001"))
    }

    @Test
    fun `POST users - 필드 누락 시 400 반환`() {
        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"홍길동"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `PUT users deactivate - 204 반환`() {
        willDoNothing().given(userService).deactivate("USR-001")

        mockMvc.perform(
            put("/users/USR-001/deactivate")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `POST users - 이메일 형식 무관하게 서비스에 전달`() {
        given(userService.register("Jane", "jane@corp.io")).willReturn("USR-002")

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Jane","email":"jane@corp.io"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string("USR-002"))
    }
}
