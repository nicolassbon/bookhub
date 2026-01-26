package com.tallerwebi.dominio.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Enumeration;

public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println(request.getRequestURL());
        System.out.println(request.getRequestURI());
        HttpSession session = request.getSession();
        Enumeration<String> attributeNames = session.getAttributeNames();
        Object usuario = null;

        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);

            if (attributeValue.equals("Usuario") || attributeValue.equals("Admin")) {
                usuario = attributeValue;
            }
        }

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        return true;
    }
}