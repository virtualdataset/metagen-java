package io.virtdata.parser;

import io.virtdata.ast.*;
import io.virtdata.generated.LambdasBaseListener;
import io.virtdata.generated.LambdasParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class LambdasBuilder extends LambdasBaseListener {
    private final static Logger logger = LoggerFactory.getLogger(LambdasBuilder.class);

    private MetagenAST model = new MetagenAST();
    private List<ErrorNode> errorNodes = new ArrayList<>();

    private Stack<LambdasParser.MetagenFlowContext> flowContexts = new Stack<>();
    private Stack<LambdasParser.ExpressionContext> expressionContexts = new Stack<>();
    private Stack<LambdasParser.MetagenCallContext> callContexts = new Stack<>();

    private LinkedList<MetagenFlow> flows = new LinkedList<>();
    private Stack<FunctionCall> calls = new Stack<FunctionCall>();

    @Override
    public void enterMetagenRecipe(LambdasParser.MetagenRecipeContext ctx) {
        logger.debug("parsing metagen lambda recipe.");

        flowContexts.clear();
        expressionContexts.clear();
        callContexts.clear();

        flows.clear();
//        calls.push(new FunctionCall("root"));
    }

    @Override
    public void exitMetagenRecipe(LambdasParser.MetagenRecipeContext ctx) {
        logger.debug("parsed metagen recipe.");
    }

    @Override
    public void enterMetagenFlow(LambdasParser.MetagenFlowContext ctx) {
        logger.debug("parsing metagen flow...");
        flowContexts.push(ctx);
        flows.push(new MetagenFlow());
        calls.clear();
        if (ctx.COMPOSE()!=null) {
            logger.warn("The 'compose' keyword is no longer needed in lambda construction. It will be deprecated in the future.");
        }
    }

    @Override
    public void exitMetagenFlow(LambdasParser.MetagenFlowContext ctx) {
        model.addFlow(flows.pop());

        flowContexts.pop();
    }

    @Override
    public void enterExpression(LambdasParser.ExpressionContext ctx) {
        expressionContexts.push(ctx);
        flows.peek().addExpression(new Expression());
        //logger.debug("parsing metagen expression.");
    }

    @Override
    public void exitLvalue(LambdasParser.LvalueContext ctx) {
        flows.peek().getLastExpression().setAssignment(new Assignment(ctx.ID().getSymbol().getText()));
    }

    @Override
    public void exitExpression(LambdasParser.ExpressionContext ctx) {
        expressionContexts.pop();
    }

    @Override
    public void enterMetagenCall(LambdasParser.MetagenCallContext ctx) {
        callContexts.push(ctx);
        calls.push(new FunctionCall());
    }

    @Override
    public void exitMetagenCall(LambdasParser.MetagenCallContext ctx) {

        FunctionCall topFunctionCall = calls.pop();
        if (calls.empty()) {
            flows.peek().getLastExpression().setCall(topFunctionCall);
        } else {
            calls.peek().addFunctionArg(topFunctionCall);
        }

        callContexts.pop();
    }

    @Override
    public void exitInputType(LambdasParser.InputTypeContext ctx) {
        calls.peek().setInputType(ctx.getText());
    }

    @Override
    public void exitFuncName(LambdasParser.FuncNameContext ctx) {
        calls.peek().setFuncName(ctx.getText());
    }

    @Override
    public void exitOutputType(LambdasParser.OutputTypeContext ctx) {
        calls.peek().setOutputType(ctx.getText());
    }

    @Override
    public void exitRef(LambdasParser.RefContext ctx) {
        calls.peek().addFunctionArg(new RefArg(ctx.ID().getText()));
    }

    @Override
    public void exitIntegerValue(LambdasParser.IntegerValueContext ctx) {
        calls.peek().addFunctionArg(new IntegerArg(Integer.valueOf(ctx.getText())));
    }

    @Override
    public void exitFloatValue(LambdasParser.FloatValueContext ctx) {
        calls.peek().addFunctionArg(new FloatArg(Float.valueOf(ctx.getText())));
    }

    @Override
    public void exitStringValue(LambdasParser.StringValueContext ctx) {
        calls.peek().addFunctionArg(new StringArg(ctx.getText().substring(1, ctx.getText().length() - 1)));
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
        super.visitErrorNode(errorNode);
        errorNodes.add(errorNode);
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }

    public boolean hasErrors() {
        return (errorNodes.size() > 0);
    }

    public List<ErrorNode> getErrorNodes() {
        return errorNodes;
    }

    public FunctionCall getMetagenCall() {
        return calls.peek().getMetagenCall(0);
    }

    public MetagenAST getModel() {
        return model;
    }
}