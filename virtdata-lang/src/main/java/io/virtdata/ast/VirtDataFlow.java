package io.virtdata.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VirtDataFlow {
    private List<Expression> expressions = new ArrayList<>();

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void addExpression(Expression expr) {
        expressions.add(expr);
    }

    public Expression getLastExpression() {
        if (expressions.size()==0) {
            throw new RuntimeException("expressions not initialized, last expression undefined");
        }
        return expressions.get(expressions.size()-1);
    }

    public Expression getFirstExpression() {
        if (expressions.size()==0) {
            throw new RuntimeException("expressions not initialized, first expression undefined.");
        }
        return expressions.get(0);
    }

    public String toString() {
        return this.expressions.stream().map(String::valueOf).collect(Collectors.joining("; "));
    }
}
