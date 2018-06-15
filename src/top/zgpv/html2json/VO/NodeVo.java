package top.zgpv.html2json.VO;

import javax.xml.soap.Node;
import java.util.*;

/**
 * The type Node vo.
 */
public class NodeVo {
    /**
     * The 节点类型.
     */
    String type = "";
    /**
     * The 文本类节点的内容.
     */
    String text = "";
    /**
     * The 元素节点的标记名.
     */
    String tag = "";
    /**
     * The 子节点.
     */
    List<NodeVo> child = new ArrayList<>();
    /**
     * The 节点的属性.
     */
    Map attrs = new HashMap(){
        @Override
        public String toString() {
            Iterator<Entry<String,String>> i = entrySet().iterator();
            if (! i.hasNext())
                return "{}";

            StringBuilder sb = new StringBuilder();
            sb.append('{');
            for (;;) {
                Entry<String,String> e = i.next();
                String key = e.getKey();
                String value = e.getValue();
                sb.append("\"" + key + "\"");
                sb.append(": ");
                sb.append(value);
                if (! i.hasNext())
                    return sb.append('}').toString();
                sb.append(',').append(' ');
            }
        }
    };

    NodeVo parent;

    public NodeVo getParent() {
        return parent;
    }

    public void setParent(NodeVo parent) {
        this.parent = parent;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets text.
     *
     * @param text the text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets tag.
     *
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets tag.
     *
     * @param tag the tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Gets child.
     *
     * @return the child
     */
    public List<NodeVo> getChild() {
        return child;
    }

    /**
     * Sets child.
     *
     * @param child the child
     */
    public void setChild(List<NodeVo> child) {
        this.child = child;
    }

    /**
     * Gets attrs.
     *
     * @return the attrs
     */
    public Map getAttrs() {
        return attrs;
    }

    /**
     * Sets attrs.
     *
     * @param attrs the attrs
     */
    public void setAttrs(Map attrs) {
        this.attrs = attrs;
    }

    public String getAllText() {
        if (this.type.equals("text")) return this.text;

        StringBuilder stringBuilder = new StringBuilder();

        for (NodeVo n : this.child) {
            stringBuilder.append(n.getAllText());
        }

        return stringBuilder.toString();
    }

    public List<NodeVo> getByTagName(String tag) {
        List<NodeVo> list = new ArrayList<>();

        if (this.tag.equals(tag)) {
            list.add(this);
            return list;
        }

        for (NodeVo n : this.child) {
            list.addAll(n.getByTagName(tag));
        }

        return list;
    }

    @Override
    public String toString() {
        return "{" +
                "\"type\": \"" + type + "\",\n" +
                "\"text\": \"" + text + "\",\n" +
                "\"tag\": \"" + tag + "\",\n" +
                "\"attrs\": " + attrs + ",\n" +
                "\"child\": " + child + "\n" +
                "}";
    }
}
