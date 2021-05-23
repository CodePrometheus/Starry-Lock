package com.star.redislock.aspect;

import com.star.redislock.annotation.SLock;
import com.star.redislock.annotation.SLockKey;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取key
 *
 * @Author: zzStar
 * @Date: 05-18-2021 18:02
 */
public class BusinessKey {

    private static final Logger logger = LoggerFactory.getLogger(BusinessKey.class);

    private ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    private ExpressionParser parser = new SpelExpressionParser();

    public String getKeyName(JoinPoint joinPoint, SLock sLock, String parameterKey) {
        List<String> keyList = new ArrayList<>();
        Method method = getMethod(joinPoint);
        List<String> spelExpressionKey = getSpelExpressionKey(sLock.keys(), method, joinPoint.getArgs());
        keyList.addAll(spelExpressionKey);

        if (!StringUtils.isEmpty(parameterKey)) {
           keyList.add(parameterKey);
        }

        return StringUtils.collectionToDelimitedString(keyList, "", "-", "");
    }

    /**
     * 反射获取方法
     *
     * @param joinPoint
     * @return
     */
    public Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 表示声明由此Method对象表示的方法的类的Class对象是否为接口
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(),
                        method.getParameterTypes());
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        return method;
    }

    /**
     * 获取spel表达式里的key
     *
     * @param keys
     * @param method
     * @param parametersValues
     * @return
     */
    public List<String> getSpelExpressionKey(String[] keys, Method method, Object[] parametersValues) {
        List<String> spelKeys = new ArrayList<>();
        for (String spelKey : spelKeys) {
            if (!ObjectUtils.isEmpty(spelKey)) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, parametersValues, discoverer);
                Object value = parser.parseExpression(spelKey).getValue(context);
                spelKeys.add(ObjectUtils.nullSafeToString(value));
            }
        }
        return spelKeys;
    }

    /**
     * 参数key
     *
     * @param parameters
     * @param parametersValues
     * @return
     */
    public List<String> getParameterKey(Parameter[] parameters, Object[] parametersValues) {
        List<String> parameterKey = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(SLockKey.class) != null) {
                SLockKey keyAnnotation = parameters[i].getAnnotation(SLockKey.class);
                if (keyAnnotation.value().isEmpty()) {
                    Object parametersValue = parametersValues[i];
                    parameterKey.add(ObjectUtils.nullSafeToString(parametersValue));
                } else {
                    StandardEvaluationContext context = new StandardEvaluationContext(parametersValues[i]);
                    Object key = parser.parseExpression(keyAnnotation.value()).getValue(context);
                    parameterKey.add(ObjectUtils.nullSafeToString(key));
                }
            }
        }
        return parameterKey;
    }
}
