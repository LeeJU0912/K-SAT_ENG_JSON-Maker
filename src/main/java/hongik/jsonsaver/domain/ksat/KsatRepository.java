package hongik.jsonsaver.domain.ksat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Repository
public class KsatRepository {
    private static final ConcurrentHashMap<String, LinkedHashSet<LinkedHashMap>> store = new ConcurrentHashMap<>();

    private static AtomicLong sequence = new AtomicLong();
    private static AtomicLong deleteSequence = new AtomicLong();

    private static final String[] type = {
            "!!! 필수 선택 !!!",
            "글의 내용 불일치",
            "글의 목적",
            "글의 요지",
            "글의 제목",
            "글의 주제",
            "나머지 넷과 다른 대상",
            "문맥상 낱말의 쓰임이 적절하지 않은 것",
            "문맥에 맞는 낱말 선택",
            "문장이 들어가기에 가장 적절한 곳",
            "밑줄친 문장이 의미하는 바",
            "빈칸 A, B에 들어갈 말",
            "빈칸에 들어갈 말",
            "심경",
            "심경 변화",
            "안내문 내용 불일치",
            "안내문 내용 일치",
            "어법",
            "요약문에 들어갈 A, B 빈칸",
            "이어질 글의 순서",
            "인물에 관한 내용 불일치",
            "표의 내용 불일치",
            "필자가 주장하는 바",
            "흐름과 관계 없는 문장",
            "A에 이어질 내용의 순서"
    };

    public Ksat save(Ksat input) {

        Ksat ksat = fixAll(input);

        LinkedHashMap<String, String> temp = new LinkedHashMap<>();

        temp.put("id", String.valueOf(sequence.incrementAndGet()));
        temp.put("질문", ksat.getQuestion());
        temp.put("원문", ksat.getTranslate());
        temp.put("본문", ksat.getMainText());
        temp.put("보기", ksat.getChoice());
        temp.put("답", ksat.getAnswer());

        if (store.containsKey(ksat.getType())) {
            store.get(ksat.getType()).add(temp);
        }
        else {
            LinkedHashSet<LinkedHashMap> makeType = new LinkedHashSet<>();
            makeType.add(temp);

            store.put(ksat.getType(), makeType);
        }

        return ksat;
    }

    public boolean deleteData(String id) {
        for (String s : type) {
            if (store.containsKey(s)) {
                for (LinkedHashMap linkedHashMap : store.get(s)) {
                    if (linkedHashMap.containsKey("id") && linkedHashMap.containsValue(id)) {
                        store.get(s).remove(linkedHashMap);
                        deleteSequence.incrementAndGet();
                        makeFile();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean chkBlankInput(Ksat ksat) {
        return ksat.getAnswer().isEmpty()
                || ksat.getQuestion().isEmpty()
                || ksat.getTranslate().isEmpty()
                || ksat.getMainText().isEmpty()
                || ksat.getChoice().isEmpty();
    }

    public void makeFile() {
        ClassPathResource filePath = new ClassPathResource("src/main/resources/dataset.json");

        try (FileWriter fileWriter = new FileWriter(filePath.getPath())) {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(store);

            fileWriter.write(jsonStr);
            fileWriter.flush();

        } catch (Exception e) {
            log.info("MakeFile Error : ", e);
        }
    }

    public Long getSize() {
        return sequence.get() - deleteSequence.get();
    }

    public void clearStore() {
        store.clear();
    }

    public Ksat fixChoiceNumberFont(Ksat ksat) {
        StringBuilder stringBuilder = new StringBuilder(ksat.getChoice());

        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == '①') {
                stringBuilder.replace(i, i + 1, "(");
                stringBuilder.insert(i + 1, "1)");
            }
            else if (stringBuilder.charAt(i) == '②') {
                stringBuilder.replace(i, i + 1, "(");
                stringBuilder.insert(i + 1, "2)");
                stringBuilder.insert(i - 1, "\n");
                i++;
            }
            else if (stringBuilder.charAt(i) == '③') {
                stringBuilder.replace(i, i + 1, "(");
                stringBuilder.insert(i + 1, "3)");
                stringBuilder.insert(i - 1, "\n");
                i++;
            }
            else if (stringBuilder.charAt(i) == '④') {
                stringBuilder.replace(i, i + 1, "(");
                stringBuilder.insert(i + 1, "4)");
                stringBuilder.insert(i - 1, "\n");
                i++;
            }
            else if (stringBuilder.charAt(i) == '⑤') {
                stringBuilder.replace(i, i + 1, "(");
                stringBuilder.insert(i + 1, "5)");
                stringBuilder.insert(i - 1, "\n");
                i++;
            }
        }
        stringBuilder.insert(stringBuilder.length(), "\n");

        ksat.setChoice(stringBuilder.toString());

        return ksat;
    }

    public Ksat fixAnswer(Ksat ksat) {
        ksat.setAnswer("(" + ksat.getAnswer() + ")");

        return ksat;
    }

    public Ksat fixAll(Ksat ksat) {

        return fixAnswer(fixChoiceNumberFont(ksat));
    }
}
