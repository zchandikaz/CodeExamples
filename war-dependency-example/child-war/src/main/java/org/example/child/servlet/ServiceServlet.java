package org.example.child.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.parent.test.ParentTestClass;

import java.io.IOException;
import java.util.UUID;


public class ServiceServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			ParentTestClass.data.put(UUID.randomUUID().toString(), "child");
			resp.getWriter().write(ParentTestClass.data.toString());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().write("child ok");
	}
}
