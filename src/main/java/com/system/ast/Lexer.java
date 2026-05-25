package com.system.ast;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private int current = 0;

    public Lexer(String source) { this.source = source; }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (current < source.length()) {
            char c = source.charAt(current);
            if (Character.isWhitespace(c)) {
                current++;
            } else if (Character.isLetter(c)) {
                tokens.add(lexIdentifierOrKeyword());
            } else if (Character.isDigit(c)) {
                tokens.add(lexNumber());
            } else {
                switch (c) {
                    case '=': tokens.add(new Token(TokenType.ASSIGN, "=")); current++; break;
                    case '+': tokens.add(new Token(TokenType.PLUS, "+")); current++; break;
                    case '-': tokens.add(new Token(TokenType.MINUS, "-")); current++; break;
                    case '*': tokens.add(new Token(TokenType.MUL, "*")); current++; break;
                    case '/': tokens.add(new Token(TokenType.DIV, "/")); current++; break;
                    case '(': tokens.add(new Token(TokenType.LPAREN, "(")); current++; break;
                    case ')': tokens.add(new Token(TokenType.RPAREN, ")")); current++; break;
                    case ';': tokens.add(new Token(TokenType.SEMICOLON, ";")); current++; break;
                    default: throw new RuntimeException("Unknown character: " + c);
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token lexIdentifierOrKeyword() {
        int start = current;
        while (current < source.length() && Character.isLetterOrDigit(source.charAt(current))) {
            current++;
        }
        String text = source.substring(start, current);
        if (text.equals("let")) return new Token(TokenType.LET, text);
        if (text.equals("print")) return new Token(TokenType.PRINT, text);
        return new Token(TokenType.IDENTIFIER, text);
    }

    private Token lexNumber() {
        int start = current;
        while (current < source.length() && Character.isDigit(source.charAt(current))) {
            current++;
        }
        return new Token(TokenType.NUMBER, source.substring(start, current));
    }
}
