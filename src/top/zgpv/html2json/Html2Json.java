package top.zgpv.html2json;

import top.zgpv.html2json.VO.NodeVo;
import top.zgpv.html2json.impl.Handler;

import java.util.ArrayList;
import java.util.Map;

public class Html2Json {
    public static NodeVo html2json(String html) throws Exception {

        html = HtmlParser.removeDoctype(html);

        ArrayList<NodeVo> stack = new ArrayList<>();

        NodeVo rootNode = new NodeVo();
        rootNode.setType("root");

        HtmlParser htmlParser = new HtmlParser(new Handler() {
            @Override
            public void start(String tag, Map<String, Object> attrs, boolean unary) {
                NodeVo node = new NodeVo();
                node.setType("element");
                node.setTag(tag);
                node.setAttrs(attrs);
                if (unary) {
                    NodeVo parent = stack.size() > 0 ? stack.get(stack.size() - 1) : rootNode;
                    node.setParent(parent);
                    parent.getChild().add(node);
                } else {
                    stack.add(node);
                }
            }

            @Override
            public void start(String tag, Map attrs) {

            }

            @Override
            public void end(String tag) {
                NodeVo node = stack.remove(stack.size() - 1);

                if (stack.size() == 0) {
                    rootNode.getChild().add(node);
                } else {
                    NodeVo parent = stack.get(stack.size() - 1);
                    node.setParent(parent);
                    parent.getChild().add(node);
                }
            }

            @Override
            public void chars(String text) {
                NodeVo textNode = new NodeVo();
                textNode.setType("text");
                textNode.setText(text);

                if (stack.size() == 0) {
                    rootNode.getChild().add(textNode);
                } else {
                    NodeVo parent = stack.get(stack.size() - 1);
                    parent.getChild().add(textNode);
                }

            }

            @Override
            public void comment(String text) {
                NodeVo textNode = new NodeVo();
                textNode.setType("comment");
                textNode.setText(text);

                if (stack.size() == 0) {
                    rootNode.getChild().add(textNode);
                } else {
                    NodeVo parent = stack.get(stack.size() - 1);
                    parent.getChild().add(textNode);
                }
            }
        });

        htmlParser.parser(html);

        return rootNode;
    }

}
