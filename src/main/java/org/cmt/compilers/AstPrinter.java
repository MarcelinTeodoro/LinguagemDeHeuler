// Arquivo: AstPrinter.java (versão final e completa)
package main.java.org.cmt.compilers;

import main.java.org.cmt.compilers.expressions.Expr;
import java.util.List;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    // --- MÉTODOS DE ENTRADA ---

    // Método principal para imprimir uma única expressão
    public String print(Expr expr) {
        return expr.accept(this);
    }

    // Novo método para imprimir uma lista de comandos (um programa)
    public String print(List<Stmt> statements) {
        StringBuilder builder = new StringBuilder();
        for (Stmt stmt : statements) {
            // CORREÇÃO: Verifique se o comando não é nulo antes de tentar imprimi-lo.
            if (stmt != null) {
                builder.append(stmt.accept(this)).append("\n");
            }
        }
        return builder.toString();
    }

    // --- VISITORS PARA EXPRESSÕES (EXPR) ---

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("= " + expr.name.lexeme, expr.value);
    }

    // Métodos para funcionalidades futuras (para o código compilar)
    @Override public String visitCallExpr(Expr.Call expr) { return "(call)"; }
    @Override public String visitGetExpr(Expr.Get expr) { return "(get)"; }
    @Override public String visitLogicalExpr(Expr.Logical expr) { return parenthesize(expr.operator.lexeme, expr.left, expr.right); }
    @Override public String visitSetExpr(Expr.Set expr) { return "(set)"; }
    @Override public String visitSuperExpr(Expr.Super expr) { return "(super)"; }
    @Override public String visitThisExpr(Expr.This expr) { return "this"; }

    // --- VISITORS PARA COMANDOS (STMT) ---

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(block");

        for (Stmt statement : stmt.statements) {
            // CORREÇÃO: Verifique se o comando não é nulo antes de processá-lo.
            if (statement != null) {
                builder.append("\n  ");
                String stmtStr = statement.accept(this);
                builder.append(stmtStr.replace("\n", "\n  "));
            }
        }

        builder.append("\n)");
        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize(";", stmt.expression);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer == null) {
            return "(var " + stmt.name.lexeme + ")";
        }
        return parenthesize("var " + stmt.name.lexeme, stmt.initializer);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        if (stmt.elseBranch == null) {
            return parenthesize("if", stmt.condition, stmt.thenBranch);
        }
        return parenthesize("if-else", stmt.condition, stmt.thenBranch, stmt.elseBranch);
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return parenthesize("while", stmt.condition, stmt.body);
    }

    // --- MÉTODO AUXILIAR PRINCIPAL ---

    private String parenthesize(String name, Object... parts) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Object part : parts) {
            builder.append(" ");
            if (part instanceof Expr) {
                builder.append(((Expr) part).accept(this));
            } else if (part instanceof Stmt) {
                builder.append(((Stmt) part).accept(this));
            } else if (part instanceof Token) {
                builder.append(((Token) part).lexeme);
            } else {
                builder.append(part);
            }
        }
        builder.append(")");
        return builder.toString();
    }
}