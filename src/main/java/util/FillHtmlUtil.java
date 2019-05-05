package util;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FillHtmlUtil {


    public static String fillHtml(String html, Map<String, Object> data) {
        for (String k : data.keySet()) {
            Object value = data.get(k);
            if (value instanceof List) {
                String label = findLabel("<\\w+ for=\"\\w+ in " + k + "\">", html);
                String targetString = findStr("<" + label + " for=\"\\w+ in " + k + "\">", "</" + label + ">", html);
                String[] forStrs = findStr("\"", k + "\"", targetString).replaceAll("\"", "").split(" ");
                String templateStr = targetString.replaceAll(" for=\"\\w+ in " + k + "\"", "");
                templateStr = fillHtml(templateStr, forStrs, (List<Object>) value);
                html = html.replace(targetString, templateStr);
            } else if (value instanceof Integer || value instanceof Long) {
                if (k.indexOf("date") != -1 || k.indexOf("time") != -1) {
                    //这个项目时间是s，所以要* 1000
                    if (value.equals(0L)) {
                        html = setValue(k, "", html);
                    } else {
                        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Long.parseLong(value.toString()) * 1000));
                        html = setValue(k, dateStr, html);
                    }
                } else {
                    html = setValue(k, value.toString(), html);
                }
            } else if (value == null) {
                html = setValue(k, "", html);
            } else {
                html = setValue(k, value.toString(), html);
            }
        }
        return html;
    }

    public static String setValue(String k, String value, String html) {
        return html.replaceAll("\\{\\{" + k + "\\}\\}", value);
    }

    public static String findLabel(String regex, String target) {
        target = findStr(regex, target);
        return target.substring(1, target.indexOf(" "));
    }

    public static String findStr(String startRegex, String target) {
        Pattern start = Pattern.compile(startRegex);
        Matcher mStart = start.matcher(target);
        if (mStart.find()) {
            target = target.substring(mStart.start(), mStart.end());
        }
        return target;
    }

    public static String findStr(String startRegex, String endRegex, String target) {
        Pattern start = Pattern.compile(startRegex);
        Pattern end = Pattern.compile(endRegex);
        Matcher mStart = start.matcher(target);
        Matcher mEnd = end.matcher(target);
        int startIdx = 0;
        int endIdx = target.length();
        if (mStart.find()) {
            startIdx = mStart.start();
        }
        if (mEnd.find(startIdx)) {
            endIdx = mEnd.end();
        }
        return target.substring(startIdx, endIdx);
    }

    public static String fillHtml(String html, String[] forStrs, List<Object> data) {
        StringBuilder result = new StringBuilder("");
        for (Object v : data) {
            if (v instanceof Map) {
                Map childData = new HashMap();
                ((Map) v).keySet().forEach(k -> {
                    childData.put(forStrs[0] + "." + k, ((Map) v).get(k));
                });
                result.append(fillHtml(html, childData));
            } else {
                result.append(html.replaceAll("\\{\\{" + forStrs[0] + "\\}\\}", v.toString()));
            }
            result.append("\n");
        }
        return result.toString();
    }

    public static void main(String args[]) throws IOException {
        InputStream in = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/test.html");
        String tempStr = IOUtils.toString(in, StandardCharsets.UTF_8);
        tempStr = fillHtml(tempStr, new HashMap<String, Object>() {{
            put("username", "zhanghao");
            put("arr", Lists.newArrayList("a", "b"));
            put("arr2", Lists.newArrayList("c", "d"));
            put("arr3", Lists.newArrayList(
                    new HashMap() {{
                        put("name", "zh");
                    }},
                    new HashMap() {{
                        put("name", "zh2");
                    }}
            ));
        }});
        in.close();
        System.out.println(tempStr);
    }

}
