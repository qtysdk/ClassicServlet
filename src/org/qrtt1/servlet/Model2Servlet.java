package org.qrtt1.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Model2Servlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getSession().setAttribute("sessionVar", new Date());
        req.setAttribute("requestVar", "I-am-a-request-var");
        req.getRequestDispatcher("/hello-template.page").forward(req, resp);
    }

}
