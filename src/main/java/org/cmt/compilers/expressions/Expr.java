// Salve este código como Expr.java
// (e apague os outros arquivos de expressão individuais)

package main.java.org.cmt.compilers.expressions;

import main.java.org.cmt.compilers.Token;
import java.util.List;

public abstract class Expr {

    // A interface do Visitor. Cada tipo de expressão terá um método visit()
    public interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitBinaryExpr(Binary expr);
        R visitCallExpr(Call expr);
        R visitGetExpr(Get expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitSetExpr(Set expr);
        R visitSuperExpr(Super expr);
        R visitThisExpr(This expr);
        R visitUnaryExpr(Unary expr);
        R visitVariableExpr(Variable expr);
    }

    // O método que permite que um "visitante" acesse o nó.
    public abstract <R> R accept(Visitor<R> visitor);

    // --- CLASSES ANINHADAS PARA CADA TIPO DE EXPRESSÃO ---

    // Expressão Binária: para operadores como +, -, *, /
    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    // Expressão Unária: para operadores como - (negação) ou !
    public static class Unary extends Expr {
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    // Expressão Literal: para valores como números, strings, true, false, nil
    public static class Literal extends Expr {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }



        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    // Expressão de Agrupamento: para parênteses (...)
    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }
    // ... suas classes Binary, Unary, Literal, Grouping ...

    // Para acesso a uma variável (ex: 'imprima a;')
    public static class Variable extends Expr {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    // Para atribuição a uma variável (ex: 'a = 1;')
    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    // Para operadores lógicos (and, or) que têm comportamento de curto-circuito
    public static class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    // Para chamadas de função (ex: 'funcao(a, b)')
    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren; // O ')' para sabermos a linha em caso de erro.
        public final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    // Para acesso a propriedades de um objeto (ex: 'objeto.propriedade')
    public static class Get extends Expr {
        public final Expr object;
        public final Token name;

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    // Para atribuição a propriedades de um objeto (ex: 'objeto.propriedade = valor')
    public static class Set extends Expr {
        public final Expr object;
        public final Token name;
        public final Expr value;

        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    // Para a palavra-chave 'this'
    public static class This extends Expr {
        public final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }
    }

    // Para a palavra-chave 'super'
    public static class Super extends Expr {
        public final Token keyword;
        public final Token method;

        public Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }
    }

    // NOTA: Outras classes de expressão como Assign, Variable, etc., seriam adicionadas aqui
    // à medida que implementamos essas funcionalidades. Por agora, estas são suficientes.
}