package org.senla.errorfreetext.scheduller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.senla.errorfreetext.client.YandexSpellerClient;
import org.senla.errorfreetext.client.dto.ResponseSpeller;
import org.senla.errorfreetext.dto.TaskStatus;
import org.senla.errorfreetext.repository.TaskRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestClientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor
@Sql(scripts = {"/sql/init.sql", "/sql/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TaskProcessSchedulerIntegrationTest {

    private final TaskProcessScheduler taskProcessScheduler;

    private final TaskRepository taskRepository;

    @MockitoSpyBean
    private YandexSpellerClient yandexSpellerClient;

    @Test
    void shouldProcessAllCreatedTasks() {

        doReturn(List.<ResponseSpeller>of())
                .when(yandexSpellerClient)
                .checkText(any());
        var status = TaskStatus.CREATED.toString();
        Long[] before = taskRepository.getNewTasksForProcess(10, status);
        var actualBefore = before.length;
        var expectedBefore = 2;

        taskProcessScheduler.runBatchProcessor();

        Long[] afterCreated = taskRepository.getNewTasksForProcess(10, status);

        var expectedAfter = 0;
        var actualAfter = afterCreated.length;

        assertEquals(expectedBefore, actualBefore);
        assertEquals(expectedAfter, actualAfter);
    }

    @Test
    void shouldSetFailedStatusWhenYandexSpellerReturnsError() {

        doThrow(new RestClientException("Yandex Speller is unavailable"))
                .when(yandexSpellerClient)
                .checkText(any());

        var createdString = TaskStatus.CREATED.toString();
        Long[] before = taskRepository.getNewTasksForProcess(10, createdString);
        var expectedBefore = 2;
        var actualBefore = before.length;

        taskProcessScheduler.runBatchProcessor();

        var string = TaskStatus.FAILED.toString();
        Long[] afterCreated = taskRepository.getNewTasksForProcess(10, string);

        var expectedAfter = 4;
        var actualAfter = afterCreated.length;

        assertEquals(expectedBefore, actualBefore);
        assertEquals(expectedAfter, actualAfter);
    }

}
