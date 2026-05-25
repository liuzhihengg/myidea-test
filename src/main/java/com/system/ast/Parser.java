package com.system.ast;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) { this.tokens = tokens; }

    // 辅助方法：查看当前 Token
    private Token peek() { return tokens.get(current); }
    // 辅助方法：消费当前 Token 并前进
    private Token advance() { if (!isAtEnd()) current++; return tokens.get(current - 1); }
    private boolean isAtEnd() { return peek().type == TokenType.EOF; }
    // 断言并消费
    private Token consume(TokenType type, String message) {
        if (peek().type == type) return advance();
        throw new RuntimeException("Parser Error: " + message);
    }

    // 1. Program -> Statement* EOF
    public Program parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return new Program(statements);
    }

    // 2. Statement -> VarDecl | PrintStmt
    private Stmt parseStatement() {
        if (peek().type == TokenType.LET) {
            advance(); // consume 'let'
            Token id = consume(TokenType.IDENTIFIER, "Expect variable name.");
            consume(TokenType.ASSIGN, "Expect '=' after variable name.");
            Expr initializer = parseExpression();
            consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
            return new VarDecl(id.lexeme, initializer);
        } else if (peek().type == TokenType.PRINT) {
            advance(); // consume 'print'
            consume(TokenType.LPAREN, "Expect '(' after print.");
            Expr expr = parseExpression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            consume(TokenType.SEMICOLON, "Expect ';' after print statement.");
            return new PrintStmt(expr);
        }
        throw new RuntimeException("Unknown statement starting with " + peek().lexeme);
    }

    // 3. Expression -> Term ( (PLUS | MINUS) Term )*
    private Expr parseExpression() {
        Expr expr = parseTerm();
        while (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS) {
            Token operator = advance();
            Expr right = parseTerm();
            expr = new BinaryOp(expr, operator, right);
        }
        return expr;
    }

    // 4. Term -> Factor ( (MUL | DIV) Factor )*
    private Expr parseTerm() {
        Expr expr = parseFactor();
        while (peek().type == TokenType.MUL || peek().type == TokenType.DIV) {
            Token operator = advance();
            Expr right = parseFactor();
            expr = new BinaryOp(expr, operator, right);
        }
        return expr;
    }

    // 5. Factor -> NUMBER | IDENTIFIER | '(' Expression ')'
    private Expr parseFactor() {
        if (peek().type == TokenType.NUMBER) {
            return new NumberLiteral(Integer.parseInt(advance().lexeme));
        } else if (peek().type == TokenType.IDENTIFIER) {
            return new VariableRef(advance().lexeme);
        } else if (peek().type == TokenType.LPAREN) {
            advance(); // consume '('
            Expr expr = parseExpression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return expr;
        }
        throw new RuntimeException("Unexpected token in expression: " + peek().lexeme);
    }
}
