package javabase.analyseproject;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DmlExtractor {

    // === 配置区：设定系统扫描的物理边界 ===
    private static final String PROJECT_ROOT = "/Users/tiaojiheng/去哪儿/source_code/tts_policy"; // 替换为你的本地代码路径
    private static final String OUTPUT_CSV = "/Users/tiaojiheng/Downloads/system_dml_audit_report.csv";

    // 存储审计结果的内部数据结构
    private static final List<AuditRecord> auditResults = new ArrayList<>();

    // 需要监听的危险 JDBC/JdbcTemplate 方法名
    private static final List<String> JDBC_TARGET_METHODS = Arrays.asList(
            "executeUpdate", "execute", "prepareStatement", "update"
    );

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 开始降维扫描项目: " + PROJECT_ROOT);
        scanProject(Paths.get(PROJECT_ROOT));
        exportToCsv();
    }

    /**
     * 全盘扫描引擎，使用 NIO 极速遍历
     */
    private static void scanProject(Path startPath) throws Exception {
        try (Stream<Path> paths = Files.walk(startPath)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString();
                if (fileName.endsWith(".xml")) {
                    extractMyBatisXml(path.toFile());
                } else if (fileName.endsWith(".java")) {
                    extractJdbcJava(path.toFile());
                }
            });
        }
    }

    /**
     * 第一维度重构：精准扫描 MyBatis XML 文件。
     * 放弃文件名猜测，利用 DOM 根节点物理判定，并精确提取映射类。
     */
    private static void extractMyBatisXml(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // 【极其关键的防御】：彻底关闭 DTD 网络校验。
            // MyBatis 的 XML 头部通常有 DOCTYPE 指向 mybatis.org。
            // 如果不关掉它，解析器会试图去网上下 DTD 文件，导致断网时脚本假死或极其缓慢！
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            // 提取根节点
            Element root = doc.getDocumentElement();

            // 【核心修正 1：物理级判定】
            // 如果根节点不是 <mapper>，说明这不是 MyBatis 文件，直接退出，绝不误杀！
            if (!"mapper".equals(root.getNodeName())) {
                return;
            }

            // 【核心修正 2：提取对应的映射类 (Mapping Class)】
            // namespace 的值，在绝大多数 MyBatis 规范中，就是 Java 接口的全限定名
            String mappingClass = root.getAttribute("namespace");
            if (mappingClass == null || mappingClass.trim().isEmpty()) {
                mappingClass = "UNKNOWN_MAPPING_CLASS";
            }

            // 遍历所有 DML 标签
            String[] dmlTags = {"insert", "update", "delete"};
            for (String tag : dmlTags) {
                NodeList nodeList = doc.getElementsByTagName(tag);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;

                        // id 属性，就是映射类里面对应的方法名
                        String methodName = element.getAttribute("id");

                        // 清洗 SQL，去除 XML 里的换行和多余空格
                        String rawSql = element.getTextContent().replaceAll("\\s+", " ").trim();

                        // 【拼装上帝视角的定位信息】
                        // 格式：[映射类名] :: [方法名]
                        String exactLocation = "Class: " + mappingClass + " :: Method: " + methodName;

                        auditResults.add(new AuditRecord(
                                "MyBatis_XML",
                                file.getName(),
                                exactLocation,       // 填入精确的类与方法映射
                                tag.toUpperCase(),
                                rawSql.length() > 200 ? rawSql.substring(0, 200) + "..." : rawSql
                        ));
                    }
                }
            }
        } catch (Exception e) {
            // 遇到非标准 XML 或残缺文件，静默跳过，保证大盘扫描不中断
            // System.err.println("解析跳过 [" + file.getName() + "]: " + e.getMessage());
        }
    }

    /**
     * 第二维度：扫描 Java 文件中的 JDBC 直连。
     * 利用 JavaParser 生成内存 AST 进行极速拦截。
     */
    private static void extractJdbcJava(File file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);

            // 祭出访问者模式 (Visitor)，在 AST 树中只猎杀 MethodCallExpr (方法调用表达式)
            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    super.visit(n, arg);
                    String methodName = n.getNameAsString();

                    if (JDBC_TARGET_METHODS.contains(methodName)) {
                        String sqlArg = "UNKNOWN_SQL_VAR";
                        if (n.getArguments().isNonEmpty()) {
                            Expression firstArg = n.getArgument(0);

                            // 1. 如果传进来的是直接拼写的字符串 "UPDATE ..."
                            if (firstArg instanceof StringLiteralExpr) {
                                sqlArg = ((StringLiteralExpr) firstArg).getValue();
                            }
                            // 2. 如果传进来的是个变量（比如 sqlBuilder.toString()）
                            else {
                                sqlArg = "[Variable Ref]: " + firstArg.toString();
                            }
                        }

                        // 粗略的语义推断
                        String upperSql = sqlArg.toUpperCase();
                        String dmlType = "UNKNOWN";
                        if (upperSql.contains("INSERT ")) dmlType = "INSERT";
                        else if (upperSql.contains("UPDATE ")) dmlType = "UPDATE";
                        else if (upperSql.contains("DELETE ")) dmlType = "DELETE";

                        // 只要有改动嫌疑，或者无法确认的变量，一律抓取入库
                        if (!dmlType.equals("UNKNOWN") || sqlArg.startsWith("[Variable")) {
                            String location = "Line: " + (n.getRange().isPresent() ? n.getRange().get().begin.line : "N/A");
                            auditResults.add(new AuditRecord(
                                    "JDBC_Java",
                                    file.getName(),
                                    location,
                                    dmlType,
                                    sqlArg.length() > 200 ? sqlArg.substring(0, 200) + "..." : sqlArg
                            ));
                        }
                    }
                }
            }, null);
        } catch (Exception e) {
            // 解析失败（比如代码语法本身有错），跳过
        }
    }

    /**
     * 将萃取矩阵强行拍平成 CSV，方便 Excel 透视分析
     */
    private static void exportToCsv() throws Exception {
        if (auditResults.isEmpty()) {
            System.out.println("未发现任何 DML 操作。");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_CSV), StandardCharsets.UTF_8)) {
            // 写入 CSV 头 (防 Excel 中文乱码，可根据系统调整)
            writer.write('\ufeff');
            writer.write("Source_Type,File_Name,Location,DML_Type,SQL_Snapshot\n");

            for (AuditRecord record : auditResults) {
                // 清洗可能的逗号或双引号破坏 CSV 格式
                String cleanSql = record.sqlSnapshot.replace("\"", "\"\"").replace("\n", " ");
                writer.write(String.format("%s,%s,%s,%s,\"%s\"\n",
                        record.sourceType, record.fileName, record.location, record.dmlType, cleanSql));
            }
        }
        System.out.println("✅ 扫描完毕！已将 " + auditResults.size() + " 条 DML 记录导出至 " + OUTPUT_CSV);
    }

    // --- 内部数据结构 ---
    static class AuditRecord {
        String sourceType;
        String fileName;
        String location;
        String dmlType;
        String sqlSnapshot;

        public AuditRecord(String sourceType, String fileName, String location, String dmlType, String sqlSnapshot) {
            this.sourceType = sourceType;
            this.fileName = fileName;
            this.location = location;
            this.dmlType = dmlType;
            this.sqlSnapshot = sqlSnapshot;
        }
    }
}
