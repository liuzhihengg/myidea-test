package com.system.ast;

import java.util.List;

public interface Visitor<T> {
    T visitProgram(Program program);
    T visitVarDecl(VarDecl stmt);
    T visitPrintStmt(PrintStmt stmt);
    T visitBinaryOp(BinaryOp expr);
    T visitNumberLiteral(NumberLiteral expr);
    T visitVariableRef(VariableRef expr);
}

// AST 基础节点
abstract class ASTNode {
    abstract <T> T accept(Visitor<T> visitor);
}

// 语句 (Statements)
abstract class Stmt extends ASTNode {}
abstract class Expr extends ASTNode {}

class Program extends ASTNode {
    final List<Stmt> statements;
    Program(List<Stmt> statements) { this.statements = statements; }
    @Override <T> T accept(Visitor<T> visitor) { return visitor.visitProgram(this); }
}

class VarDecl extends Stmt {
    final String name;
    final Expr initializer;
    VarDecl(String name, Expr initializer) { this.name = name; this.initializer = initializer; }
    @Override <T> T accept(Visitor<T> visitor) { return visitor.visitVarDecl(this); }
}

class PrintStmt extends Stmt {
    final Expr expression;
    PrintStmt(Expr expression) { this.expression = expression; }
    @Override <T> T accept(Visitor<T> visitor) { return visitor.visitPrintStmt(this); }
}

// 表达式 (Expressions)
class BinaryOp extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;
    BinaryOp(Expr left, Token operator, Expr right) {
        this.left = left; this.operator = operator; this.right = right;
    }
    @Override <T> T accept(Visitor<T> visitor) { return visitor.visitBinaryOp(this); }
}

class NumberLiteral extends Expr {
    final int value;
    NumberLiteral(int value) { this.value = value; }
    @Override <T> T accept(Visitor<T> visitor) { return visitor.visitNumberLiteral(this); }
}

class VariableRef extends Expr {
    final String name;
    VariableRef(String name) { this.name = name; }
    @Override <T> T accept(Visitor<T> visitor) { return visitor.visitVariableRef(this); }
}
