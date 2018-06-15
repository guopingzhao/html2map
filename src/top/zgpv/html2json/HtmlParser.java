package top.zgpv.html2json;

import top.zgpv.html2json.impl.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The type Html parser.
 */
public class HtmlParser {
    /**
     * 匹配开始标记正则
     */
    private Pattern startTag = Pattern.compile("^<[A-z0-9]([^>]| \n)*>\n?");
    /**
     * 匹配结束标记正则
     */
    private Pattern endTag = Pattern.compile("^<\\/[A-z0-9][^>]*>\n?");
    /**
     * 匹配开始标记属性正则
     */
    private Pattern attr = Pattern.compile("([A-z][A-z0-9-_:]*(?=(=|\\s))|=|'([^']|\\s)*'|\"([^\"]|\\s)*\"|\\S+)");

    /**
     * 不存在子节点的元素
     */
    private Map<String, Boolean> empty = makeMap("area,base,basefont,br,col,frame,hr,img,input,link,meta,param,embed,command,keygen,source,track,wbr");

    /**
     * 不存在子节点的元素
     */
    private Map<String, Boolean> block = makeMap("a,address,article,applet,aside,audio,blockquote,button,canvas,center,dd,del,dir,div,dl,dt,fieldset,figcaption,figure,footer,form,frameset,h1,h2,h3,h4,h5,h6,header,hgroup,hr,iframe,ins,isindex,li,map,menu,noframes,noscript,object,ol,output,p,pre,section,script,table,tbody,td,tfoot,th,thead,tr,ul,video");

    /**
     * 不存在子节点的元素
     */
    private Map<String, Boolean> inline = makeMap("abbr,acronym,applet,b,basefont,bdo,big,br,button,cite,code,del,dfn,em,font,i,iframe,img,input,ins,kbd,label,map,object,q,s,samp,script,select,small,span,strike,strong,sub,sup,textarea,tt,u,var");

    /**
     * 如果没有结束标记会自关闭的元素
     */
    private Map<String, Boolean> closeSelf = makeMap("colgroup,dd,dt,li,options,p,td,tfoot,th,thead,tr");

    /**
     * 元素填充属性
     */
    private Map<String, Boolean> fillAttrs = makeMap("checked,compact,declare,defer,disabled,ismap,multiple,nohref,noresize,noshade,nowrap,readonly,selected");

    /**
     * 样式和脚本元素
     */
    private Map<String, Boolean> special = makeMap("script,style");

    /**
     * 只会出现一次的元素
     */
    private Map<String, Boolean> one = makeMap("html,head,body,title");

    /**
     * 对每个步骤的子处理方法
     */
    private Handler handler;

    private List<String> stack = new ArrayList<>();

    /**
     * Instantiates a new Html parser.
     */
    public HtmlParser() {
    }

    /**
     * Instantiates a new Html parser.
     *
     * @param handler the handler
     */
    public HtmlParser(Handler handler) {
        this.handler = handler;
    }

    /**
     * Parser.
     *
     * @param source the 需要编译的html字符串
     */
    public void parser(String source) throws Exception {
        String html = source;
        Matcher matcher;
        int index;
        boolean isChars;

        while (html != null && !html.equals("")) {
            isChars = true;
            if (html.indexOf("<!--") == 0) {
                index = html.indexOf("-->");
                if (index >= 0) {
                    if (handler != null)
                        handler.comment(html.substring(4, index));
                    html = html.substring(index + 3);
                    isChars = false;
                }

            } else if (html.indexOf("</") == 0) {
                matcher = endTag.matcher(html);
                if (matcher.find()) {
                    html = html.substring(matcher.group(0).length());
                    parseEndTag(matcher.group(0));
                    isChars = false;
                }

            } else if (html.indexOf("<") == 0) {
                matcher = startTag.matcher(html);
                if (matcher.find()) {
                    html = html.substring(matcher.group(0).length());
                    parseStartTag(matcher.group(0));
                    isChars = false;
                }

            } else if (isChars) {
                index = html.indexOf("<");
                if (index > 0) {
                    String text = html.substring(0, index);
                    html = html.substring(index);
                    if (handler != null) handler.chars(text);
                } else {
                    throw new Exception("格式错误");
                }
            }
        }

    }

    /**
     * 移除html doctype.
     *
     * @param html the html
     * @return the string
     */
    public static String removeDoctype(String html) {
        return html.replace("<\\?xml.*\\?>\\n", "").replace("<!(?i)DOCTYPE.*\\>\\n", "");
    }

    /**
     * 处理开始标记
     *
     * @param tag the html
     */
    private void parseStartTag(String tag) {
        boolean isClose = tag.endsWith("/>");
        Pattern tagNameRegexp = Pattern.compile("[A-z][^\\s>]*");
        Map attrs = new HashMap();

        tag = tag.replace("<", "").replace("\n", " ");

        if (isClose) {
            tag = tag.replace("/>", "");
        } else {
            tag = tag.replace(">", "");
        }


        Matcher matcher = tagNameRegexp.matcher(tag);
        matcher.find();

        String tagName = matcher.group(0);

        tag = tag.replace(tagName, "").trim();

        List<String> all = findAll(attr, tag);

        if (!all.isEmpty()) {
            for (int i = 0; i < all.size(); ) {
                if (all.size() > i + 1) {
                    if (all.get(i + 1).equals("=")) {
                        if (all.size() > i + 2) {
                            attrs.put(all.get(i), all.get(i + 2));
                            i += 3;
                        } else {
                            i += 2;
                        }
                    } else {
                        attrs.put(all.get(i), true);
                        i++;
                    }
                } else {
                    attrs.put(all.get(i), true);
                    i++;
                }
            }
        }

        if (!isClose) {
            stack.add(tagName);
        }

        if (handler != null) {
            handler.start(tagName, attrs, isClose);
        }
    }

    /**
     * 处理结束标记
     *
     * @param tag the html
     */
    private void parseEndTag(String tag) throws Exception {
        if (stack.size() <= 0) throw new Exception("html格式错误");
        tag = tag.replace("</", "").replace(">", "").replace("\n", " ").trim();
        String removeTag = stack.remove(stack.size() - 1);

        while (!(tag.equals(removeTag))) {
            if (!tag.equals("") && handler != null) {
                handler.end(removeTag);
            }
            if (stack.size() > 0) {
                removeTag = stack.remove(stack.size() - 1);
            } else {
                throw new Exception("html格式错误");
            }
        }

        if ((tag.equals(removeTag) || !tag.equals("")) && handler != null) {
            handler.end(tag);
        }
    }

    private Map makeMap(String str) {

        Map<String, Boolean> map = new HashMap<>();
        String[] strs = str.split(",");

        for (String s : strs) {
            map.put(s, true);
        }

        return map;
    }

    private String getStackLast() {
        if (stack.size() > 0) {
            return stack.get(stack.size() - 1);
        }
        return null;
    }

    private List<String> findAll(Pattern reg, String str) {
        List<String> list = new ArrayList<>();

        Matcher matcher = reg.matcher(str);

        while (matcher.find()) {
            list.add(matcher.group());
        }

        return list;
    }
}
