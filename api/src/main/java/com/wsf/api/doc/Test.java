package com.wsf.api.doc;

import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * open
 * SoulLose
 * 2022-05-31 09:23
 */
public class Test {
    public static void main(String[] args) {
        outWord();
    }
    
    public void createWordTemplate() {
        XWPFDocument document= new XWPFDocument();
    }
    
    public static void outWord(){
        XWPFTemplate template = XWPFTemplate.compile("template.docx").render(
                new HashMap<String, Object>(){{
                    put("title", "Hi, poi-tl Word模板引擎");
                }});
        try {
            template.writeAndClose(new FileOutputStream("output.docx"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
