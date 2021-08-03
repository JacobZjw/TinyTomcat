package util;

import cn.hutool.core.util.StrUtil;
import http.Request;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JwZheng
 * @date 2021/7/31 20:08
 */
@Slf4j
public class TemplateResolver {
    public static final Pattern regex = Pattern.compile("\\$\\{(.*?)\\}");

    public static String resolve(String content, Request request) {
        Matcher matcher = regex.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeHolder = matcher.group(1);
            if (placeHolder.indexOf('.') == -1) {
                log.error("模板解析失败");
                return null;
            }
            Scope scope = Scope
                    .valueOf(
                            placeHolder.substring(0, placeHolder.indexOf('.'))
                                    .replace("Scope", "")
                                    .toUpperCase());
            String key = placeHolder.substring(placeHolder.indexOf('.') + 1);
            Object obj = null;
            String[] segments = key.split("\\.");
            if (Scope.REQUEST.equals(scope)) {
                obj = request.getAttribute(segments[0]);
            } else if (Scope.SESSION.equals(scope)) {
                obj = request.getSession().getAttribute(segments[0]);
            } else {
                log.error("模板解析失败");
                return null;
            }
            if (segments.length > 1) {
                try {
                    obj = parse(obj, segments, 1);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            if (obj == null) {
                matcher.appendReplacement(sb, "");
            } else {
                matcher.appendReplacement(sb, obj.toString());
            }
        }
        matcher.appendTail(sb);
        String result = sb.toString();
        return result.length() == 0 ? content : result;
    }

    private static Object parse(Object value, String[] segments, int index) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (index == segments.length) {
            return value;
        }
        Method method = value.getClass().getMethod("get" + capitalize(segments[index]));
        return parse(method.invoke(value), segments, index + 1);
    }

    private static String capitalize(String str) {
        if (StrUtil.isEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        if (Character.isUpperCase(chars[0])) {
            return str;
        }
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

}
