package bupt.weibo.handler;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//public class LockedAuthenticationEntryPoint implements AuthenticationEntryPoint {
//
//    @Override
//    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
//        System.out.println("异常处理" + e.getMessage());
//        System.out.println("具体信息" + e.toString());
//        if (e instanceof LockedException) {
//            response.setCharacterEncoding("UTF-8");
//            System.out.println(e.getClass());
//            response.sendError(401, "该用户被锁定: " + e.getMessage());
//        }
//        response.sendRedirect("/login");
//    }
//}
