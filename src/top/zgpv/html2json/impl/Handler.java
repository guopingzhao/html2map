package top.zgpv.html2json.impl;

import java.util.Map;

public interface Handler {
    public void start(String tag, Map<String, Object> attrs, boolean unary);
    public void start(String tag, Map<String, Object> attrs);
    public void end(String tag);
    public void chars(String text);
    public void comment(String text);
}
