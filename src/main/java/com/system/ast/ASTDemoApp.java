package com.system.ast;

import java.util.List;

public class ASTDemoApp {
    public static void main(String[] args) {
        String code = "let a = 10;\n" +
                "let b = a * (2 + 3);\n" +
                "print(b);\n";

        System.out.println("1. 源代码:\n" + code);

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        System.out.println("2. 词法分析 (Tokens): " + tokens);

        Parser parser = new Parser(tokens);
        Program ast = parser.parse();
        System.out.println("3. 语法树 (AST) 构建完毕！");

        Interpreter interpreter = new Interpreter();
        System.out.println("4. 执行结果:");
        interpreter.execute(ast); // 应该输出 50
    }
}
