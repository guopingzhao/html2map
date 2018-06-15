import top.zgpv.html2json.GbkToUtf8;
import top.zgpv.html2json.Html2Json;
import top.zgpv.html2json.VO.NodeVo;

import java.io.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        StringBuilder html = new StringBuilder();


        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("租房合同样例.html"), "gbk"));

        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            html.append(line);
            html.append("\n");
        }

        bufferedReader.close();

        NodeVo rootNode = Html2Json.html2json(html.toString());

        StringBuilder stringBuilder = new StringBuilder();

        List<NodeVo> h1 = rootNode.getByTagName("h1");
        BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("result.txt"), "utf-8"));

        for (NodeVo h : h1) {
            NodeVo parent = h.getParent();
            stringBuilder.append("标题一：" + h.getAllText());
            stringBuilder.append("\n");

            List<NodeVo> ps = parent.getByTagName("p");
            stringBuilder.append("内容：\n");
            for (NodeVo p : ps) {
                stringBuilder.append(p.getAllText() + "\n");
            }

        }

        fileWriter.write(GbkToUtf8.format(stringBuilder.toString()));
        fileWriter.flush();
        fileWriter.close();
    }


}
