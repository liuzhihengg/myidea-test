import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestTest {

    /**
     * 方法参数转化成类变量。例如下面这个
     * private List<Map.Entry<String, Map<String, UnionPackageCabinPrice>>> flightBatch;
     *
     * private Map<String, String> userLabel;
     *
     * private Map<String, String> abTest;
     *
     * private Map<String, Object> dataExt;
     *
     * private Map<String, Map<String, Float>> fdData;
     *
     * private Map<String, Long2ObjectMap<CtripUnionPriceData>> ctripUnionPrice;
     */
    @Test
    public void test1() {
        String parameterList = "List<Map.Entry<String, Map<String, UnionPackageCabinPrice>>> flightBatch, " +
                "Map<String, String> userLabel, " +
                "Map<String, String> abTest, " +
                "Map<String, Object> dataExt, " +
                "Map<String, Map<String, Float>> fdData, " +
                "Map<String, Long2ObjectMap<CtripUnionPriceData>> ctripUnionPrice";

        // Remove any extra quotes and plus signs
        parameterList = parameterList.replace("\"", "").replace("+", "");

        // Initialize variables for parsing
        int angleBracketLevel = 0;
        StringBuilder currentParam = new StringBuilder();
        List<String> parameters = new ArrayList<>();

        // Parse parameters by commas not within angle brackets
        for (char c : parameterList.toCharArray()) {
            if (c == '<') {
                angleBracketLevel++;
            } else if (c == '>') {
                angleBracketLevel--;
            } else if (c == ',' && angleBracketLevel == 0) {
                parameters.add(currentParam.toString().trim());
                currentParam.setLength(0);
                continue;
            }
            currentParam.append(c);
        }
        // Add the last parameter
        if (currentParam.length() > 0) {
            parameters.add(currentParam.toString().trim());
        }

        // Convert parameters to private fields
        for (String param : parameters) {
            int lastSpaceIndex = param.lastIndexOf(' ');
            if (lastSpaceIndex == -1) {
                System.out.println("// Unable to parse parameter: " + param);
                continue;
            }
            String type = param.substring(0, lastSpaceIndex).trim();
            String name = param.substring(lastSpaceIndex + 1).trim();
            System.out.println("private " + type + " " + name + ";");
            System.out.println("");
        }
    }

    @Test
    public void test2() {
        String parameterList = "List<Map.Entry<String, Map<String, UnionPackageCabinPrice>>> flightBatch, " +
                "Map<String, String> userLabel, " +
                "Map<String, String> abTest, " +
                "Map<String, Object> dataExt, " +
                "Map<String, Map<String, Float>> fdData, " +
                "Map<String, Long2ObjectMap<CtripUnionPriceData>> ctripUnionPrice";

        String variableName = "condition";

        // Remove any extra quotes and plus signs
        parameterList = parameterList.replace("\"", "").replace("+", "");

        // Initialize variables for parsing
        int angleBracketLevel = 0;
        StringBuilder currentParam = new StringBuilder();
        List<String> parameters = new ArrayList<>();

        // Parse parameters by commas not within angle brackets
        for (char c : parameterList.toCharArray()) {
            if (c == '<') {
                angleBracketLevel++;
            } else if (c == '>') {
                angleBracketLevel--;
            } else if (c == ',' && angleBracketLevel == 0) {
                parameters.add(currentParam.toString().trim());
                currentParam.setLength(0);
                continue;
            }
            currentParam.append(c);
        }
        // Add the last parameter
        if (currentParam.length() > 0) {
            parameters.add(currentParam.toString().trim());
        }

        // Generate assignments
        for (String param : parameters) {
            int lastSpaceIndex = param.lastIndexOf(' ');
            if (lastSpaceIndex == -1) {
                System.out.println("// Unable to parse parameter: " + param);
                continue;
            }
            String type = param.substring(0, lastSpaceIndex).trim();
            String name = param.substring(lastSpaceIndex + 1).trim();
            // Capitalize the first letter of the variable name for the getter method
            String capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
            System.out.println(type + " " + name + " = " + variableName + ".get" + capitalizedName + "();");
        }
    }


}