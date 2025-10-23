package org.lld.compositedesignpattern.filesystem;

public class CalculatorDemo {
    static interface AirthmeticExpression{
        int evaluate();
    }
    static class Result implements AirthmeticExpression{
        int value;
        public Result(int value){
            this.value = value;
        }
        @Override
        public int evaluate() {
            return value;
        }
    }
    static enum Operation{
        ADD,SUB,MUL,DIV;
    }

    static class Expression implements AirthmeticExpression{
        Operation operation;
        AirthmeticExpression leftExpression;
        AirthmeticExpression rightExpression;
        public Expression(AirthmeticExpression leftExpression, AirthmeticExpression rightExpression, Operation operation){
            this.leftExpression = leftExpression;
            this.rightExpression = rightExpression;
            this.operation = operation;
        }
        @Override
        public int evaluate() {
            int result = switch (operation) {
                case ADD -> leftExpression.evaluate() + rightExpression.evaluate();
                case SUB -> leftExpression.evaluate() - rightExpression.evaluate();
                case MUL -> leftExpression.evaluate() * rightExpression.evaluate();
                case DIV -> leftExpression.evaluate() / rightExpression.evaluate();
            };
            System.out.println("Expression result is  "+ result);
            return result;
        }
    }

    public static void main(String[] args) {
        Result result1 = new Result(10);
        Result result2 = new Result(5);
        Result result3 = new Result(8);
        Expression expression1 = new Expression(result1, result2, Operation.DIV);
        Expression expression2 = new Expression(expression1, result3, Operation.MUL);
        expression2.evaluate();

    }
}


