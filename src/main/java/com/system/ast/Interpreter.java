package com.system.ast;

import java.util.HashMap;
import java.util.Map;

public class Interpreter implements Visitor<Integer> {
    // 模拟内存中的变量环境（符号表的最简形式）
    private final Map<String, Integer> environment = new HashMap<>();

    public void execute(Program program) {
        program.accept(this);
    }

    @Override
    public Integer visitProgram(Program program) {
        for (Stmt stmt : program.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Integer visitVarDecl(VarDecl stmt) {
        int value = stmt.initializer.accept(this);
        environment.put(stmt.name, value);
        return null; // 语句不返回值
    }

    @Override
    public Integer visitPrintStmt(PrintStmt stmt) {
        int value = stmt.expression.accept(this);
        System.out.println(">>> Output: " + value);
        return null;
    }

    @Override
    public Integer visitBinaryOp(BinaryOp expr) {
        int leftVal = expr.left.accept(this);
        int rightVal = expr.right.accept(this);

        switch (expr.operator.type) {
            case PLUS: return leftVal + rightVal;
            case MINUS: return leftVal - rightVal;
            case MUL: return leftVal * rightVal;
            case DIV: return leftVal / rightVal;
        }
        throw new RuntimeException("Unknown operator");
    }

    @Override
    public Integer visitNumberLiteral(NumberLiteral expr) {
        return expr.value;
    }

    @Override
    public Integer visitVariableRef(VariableRef expr) {
        if (!environment.containsKey(expr.name)) {
            throw new RuntimeException("Undefined variable: " + expr.name);
        }
        return environment.get(expr.name);
    }
}
