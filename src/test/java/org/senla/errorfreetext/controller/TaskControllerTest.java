package org.senla.errorfreetext.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.senla.errorfreetext.controller.dto.TaskRequest;
import org.senla.errorfreetext.controller.dto.TaskResponse;
import org.senla.errorfreetext.controller.dto.TaskResultResponse;
import org.senla.errorfreetext.exception.EntityNotFoundException;
import org.senla.errorfreetext.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void createTaskShouldReturnTaskResponseWhenRequestIsValid() throws Exception {
        TaskRequest request = new TaskRequest("Some text", "EN");

        TaskResponse response = new TaskResponse(
                1L,
                1
        );

        given(taskService.createTask(any(TaskRequest.class))).willReturn(response);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(1L))
                .andExpect(jsonPath("$.contentCount").exists());
    }

    @Test
    void createTaskShouldReturnBadRequestWhenTextIsInvalid() throws Exception {
        TaskRequest request = new TaskRequest("12", "EN");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("40001"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.errorMessage").value(
                        org.hamcrest.Matchers.containsString(
                                "Текст должен содержать минимум 3 символа и не может содержать только спецсимволы и цифры"
                        )
                ))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createTaskShouldReturnBadRequestWhenLangIsInvalid() throws Exception {
        TaskRequest request = new TaskRequest("Valid text", "DE");

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("40001"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.errorMessage").value(
                        org.hamcrest.Matchers.containsString(
                                "Параметр языка может быть только 'EN' или 'RU'"
                        )
                ))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getTaskShouldReturnResultWhenTaskExists() throws Exception {
        TaskResultResponse result = TaskResultResponse.builder()
                .status("COMPLETED")
                .data("Processed text")
                .error(null)
                .build();

        given(taskService.getTaskResult(1L)).willReturn(result);

        mockMvc.perform(get("/tasks/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data").value("Processed text"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void getTaskShouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        long taskId = 42L;

        EntityNotFoundException ex = new EntityNotFoundException(
                "Task with id: " + taskId + " not found"
        );

        given(taskService.getTaskResult(taskId)).willThrow(ex);

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("40401"))
                .andExpect(jsonPath("$.path").value("/tasks/" + taskId))
                .andExpect(jsonPath("$.errorMessage").value(
                        org.hamcrest.Matchers.containsString("Task with id: " + taskId + " not found")
                ))
                .andExpect(jsonPath("$.timestamp").exists());
    }


}

