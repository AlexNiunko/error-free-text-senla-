package org.senla.errorfreetext.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.senla.errorfreetext.client.dto.ResponseSpeller;
import org.senla.errorfreetext.dto.ContentDto;
import org.senla.errorfreetext.service.impl.ContentServiceImpl;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
class ContentServiceImplTest {

    private static final String DATA_FOR_DIVIDE = """
            В истории Центральной и Восточной Европы немного найдется правителей, чья жизнь была бы столь же насыщена
            драматическими поворотами,военными триумфами и политическими интригами, как жизнь великого князя литовского
            Витовта. Его фигура возвышается над временем подобно исполину, соединившему в себе черты средневекового
            рыцаря, дальновидного дипломата и безжалостного государственника. Правитель, чья власть простиралась от
            Балтийского до Черного моря, человек, трижды менявший веру ради достижения целей, тесть московского князя и
            сюзерен огромных русских земель, союзник и враг Тевтонского ордена, двоюродный брат польского короля — всё
            это Витовт, или, как почтительно называли его современники, Витовт Великий .
            """;
    private final ContentService contentService = new ContentServiceImpl(100);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDivideData() {
        List<String> divide = contentService.divide(DATA_FOR_DIVIDE);
        String actual = String.join("", divide);
        assertEquals(DATA_FOR_DIVIDE, actual);
    }

    @Test
    void testBuildData() {
        List<ContentDto> list = List.of(
                ContentDto.builder().data("Это предложение должно быть последним.").position(3).build(),
                ContentDto.builder().data("А вот это самым первым!").position(1).build(),
                ContentDto.builder().data("Хочу быть посередине...").position(2).build());
        String expected = "А вот это самым первым!Хочу быть посередине...Это предложение должно быть последним.";

        var actual = contentService.buildData(list);
        assertEquals(expected, actual);

    }

    @ParameterizedTest
    @CsvSource(
            {
                    "Это предложение должно быть после7дним., true",
                    "Это предложение должно быть последним., false",
            }
    )
    void testExistDigit(String content, boolean expected) {
        assertEquals(expected, contentService.existDigit(content));

    }

    @ParameterizedTest
    @CsvSource(
            {
                    "Это предложение должно быть последним., false",
                    "Это https://www.google.com/ последним., true",
                    "Это www.google.com/ последним., true",
                    "Это google.org последним., true"
            }
    )
    void testExistUrl(String content, boolean expected) {
        assertEquals(expected, contentService.existURL(content));
    }

    @Test
    void testProcess() throws IOException {
        ClassPathResource response = new ClassPathResource("json/response.json");
        ClassPathResource input = new ClassPathResource("json/input.json");
        ClassPathResource output = new ClassPathResource("json/output.json");

        List<ResponseSpeller> resp = objectMapper.readValue(
                response.getInputStream(),
                new TypeReference<>() {
                }
        );

        JsonNode inputRoot = objectMapper.readTree(input.getInputStream());
        JsonNode outputRoot = objectMapper.readTree(output.getInputStream());
        String inputText = inputRoot.get("text").asText();
        String expected = outputRoot.get("text").asText();

        var actual = contentService.process(resp, inputText);
        assertEquals(expected, actual);


    }

}
