package javabase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class PolicyDptArrCounter {

    public static void main(String[] args) throws Exception {

        Path xmlPath = Paths.get("/Users/tiaojiheng/Downloads/qunar_28e710c2929471425de0b92f75f70e92.xml");
        XMLInputFactory factory = XMLInputFactory.newInstance();

        // 用 HashSet 记录唯一的 dpt-arr 组合
        Set<String> uniqueKeys = new HashSet<>();

        try (InputStream in = Files.newInputStream(xmlPath)) {
            XMLStreamReader reader = factory.createXMLStreamReader(in);

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT &&
                        "Policy".equals(reader.getLocalName())) {

                    String dpt = reader.getAttributeValue(null, "dpt");
                    String arr = reader.getAttributeValue(null, "arr");

                    if (dpt != null && arr != null) {
                        uniqueKeys.add(dpt.trim() + "-" + arr.trim());
                    }
                }
            }
            reader.close();
        }

        // 结果输出
        System.out.println("唯一 dpt-arr 组合数量: " + uniqueKeys.size());
        uniqueKeys.stream().sorted().forEach(System.out::println);
    }
}
