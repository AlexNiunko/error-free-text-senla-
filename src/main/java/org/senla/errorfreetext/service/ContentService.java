package org.senla.errorfreetext.service;


import java.util.List;
import org.senla.errorfreetext.client.dto.ResponseSpeller;
import org.senla.errorfreetext.dto.ContentDto;

public interface ContentService {

    List<String> divide(String data);

    String buildData(List<ContentDto> parts);

    boolean existDigit(String content);

    boolean existURL(String content);

    String process(List<ResponseSpeller> list, String data);

}
