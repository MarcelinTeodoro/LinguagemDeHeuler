package main.java.org.cmt.compilers.sintatico;

import main.java.org.cmt.compilers.sintatico.expressions.Expr;
import java.util.List;

/**
 * Hierarquia de instruções/declarações (Stmt) para a linguagem. Cada nó possui
 * um método `accept` para o Visitor que permite processar a AST de forma
 * separada da sua estrutura de dados.
 *
 * Nós incluídos: Block, Expression, Print, Var, If, While.
 */
public abstract class Stmt {

    public interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitForStmt(For stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    public static class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    public static class Var extends Stmt {
        public final main.java.org.cmt.compilers.lexico.Token name;
        public final main.java.org.cmt.compilers.lexico.Token typeToken; // optional type annotation
        public final Expr initializer;

        public Var(main.java.org.cmt.compilers.lexico.Token name, main.java.org.cmt.compilers.lexico.Token typeToken, Expr initializer) {
            this.name = name;
            this.typeToken = typeToken;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class For extends Stmt {
        public final main.java.org.cmt.compilers.lexico.Token iterator;
        public final Expr iterable;
        public final Stmt body;

        public For(main.java.org.cmt.compilers.lexico.Token iterator, Expr iterable, Stmt body) {
            this.iterator = iterator;
            this.iterable = iterable;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Stmt body, Expr condition) {
            this.body = body;
            this.condition = condition;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }
}
