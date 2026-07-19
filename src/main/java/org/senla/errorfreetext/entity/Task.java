package org.senla.errorfreetext.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task extends AbstractEntity {

    private String lang;
    private TaskStatus status;
    private List<TaskContent> taskContents;
    private List<TaskError> taskErrors;

}
