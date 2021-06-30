package com.fly.serverless.util;

import lombok.experimental.UtilityClass;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/29
 */
@UtilityClass
public class ExpressionUtils {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    @SuppressWarnings("unchecked")
    public static <T> T parse(String expression, Object root) {
        Object value = null;

        if (root instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) root;
            value = map.get(expression);
        }

        if (value != null) {
            return (T) value;
        }

        //这里可以增加缓存


        Expression exp = PARSER.parseExpression(expression);
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        if (root instanceof Map) {
            context.addPropertyAccessor(new MapAccessor());
        }

        return (T) exp.getValue(context);
    }

}
