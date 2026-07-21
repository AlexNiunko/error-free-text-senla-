package org.senla.errorfreetext.client.dto;

import java.util.List;

public record ResponseSpeller(
        int code,
        int pos,
        int row,
        int col,
        int len,
        String word,
        List<String> s

) {
}
