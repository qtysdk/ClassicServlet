package org.qrtt1.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.midi.Soundbank;

@SuppressWarnings("serial")
public class StringTemplateServlet extends HttpServlet {

    private static final String PAGE_PATH = "/WEB-INF/pages";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // 將 URI 轉換成內部的路徑
        String pagePath = PAGE_PATH + req.getRequestURI().substring(req.getContextPath().length())
                .replace(".page", ".html");

        System.out.println("Context Path: " + req.getContextPath());
        System.out.println("Request URI: " + req.getRequestURI());
        System.out.println("Page Path: " + pagePath);

        // 確認路徑真的有檔案存在
        String path = this.getServletContext().getRealPath(pagePath);
        File file = new File(path);
        if (!file.exists()) {
            resp.sendError(404);
            return;
        }

        // 讀取文字樣版
        String template = getTemplate(file);
        resp.addHeader("Content-Type", "text/html; charset=utf-8");

        // 插入變數
        StringBuffer sb = applyVariables(template, req);
        resp.getWriter().println(sb.toString());
    }

    private StringBuffer applyVariables(String template, HttpServletRequest req) {
        // 找出所有 ${ } 形式的字串
        Pattern placeHolders = Pattern.compile("(\\$\\{([^{]+)\\})");
        Matcher m = placeHolders.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        // 將找到的字串換成 scope object 內的值 (如果查得到的話)
        while (m.find()) {
            String placeHolder = m.group();
            String variableName = m.group(2).trim();
            System.out.println("" + placeHolder + " -> name[" + variableName + "]");
            m.appendReplacement(sb, findValue(req, variableName));
        }
        m.appendTail(sb);
        return sb;
    }

    private String findValue(HttpServletRequest req, String variableName) {
        // 取值的設計的優先序為 (這就看 template engine 的作者怎麼設計了)
        // request parameter
        // session object
        // request object
        
        if (req.getParameter(variableName) != null) {
            return req.getParameter(variableName);
        }
        if (req.getSession().getAttribute(variableName) != null) {
            return req.getSession().getAttribute(variableName).toString();
        }
        if (req.getAttribute(variableName) != null) {
            return req.getAttribute(variableName).toString();
        }
        
        // 找不到就換成 unknown
        return "unknown::" + variableName + "::";
    }

    private String getTemplate(File file) throws FileNotFoundException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[1024 * 1024];
            while (true) {
                int r = input.read(buffer);
                if (r == -1) {
                    break;
                }
                output.write(buffer, 0, r);
            }
        }
        return new String(output.toByteArray(), "utf-8");
    }

}
