package org.senla.errorfreetext.repositoy;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.senla.jooq.generated.tables.Task;
import org.springframework.stereotype.Component;

import static org.senla.jooq.generated.tables.Task.TASK;

@Component
@RequiredArgsConstructor
public class TaskRepository {

    private final DSLContext dsl;

    public Long saveTask(Task task){


        return 0L;
    }




}
