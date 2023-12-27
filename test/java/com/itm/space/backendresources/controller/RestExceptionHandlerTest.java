package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.exception.BackendResourcesException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.ws.rs.core.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class RestExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testHandleException(){
        BackendResourcesException exception = new BackendResourcesException("Test Exception", HttpStatus.INTERNAL_SERVER_ERROR);
        // создание объекта, со статусом. В данном контексте, HttpStatus.INTERNAL_SERVER_ERROR используется
        // для установки статуса исключения BackendResourcesException в тесте. Затем этот статус сравнивается с фактическим статусом,
        // возвращаемым методом handleException(), чтобы убедиться, что обработчик исключений правильно обрабатывает
        // исключение и возвращает ожидаемый статус код.
        RestExceptionHandler exceptionHandler = new RestExceptionHandler();
        // В данном тесте создается экземпляр класса RestExceptionHandler для того, чтобы вызвать его метод handleException()
        // и проверить его поведение при обработке исключения.
        ResponseEntity<String> response = exceptionHandler.handleException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Test Exception", response.getBody());
    }
    @Test
    // проверка обработки неверных аргументов в методе контроллера
    @WithMockUser(roles = "MODERATOR")
    public void testHandleInvalidArgument() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        //Выполняется запрос по этому пути
                        .contentType(MediaType.APPLICATION_JSON)
                        //устанавливает тип контента запроса как "application/json"
                        .content("{}"))
                // а тут просто пустое тело Json'a
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        // ожидаемый результат статуса
    }
}