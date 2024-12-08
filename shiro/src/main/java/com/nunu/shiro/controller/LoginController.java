package com.nunu.shiro.controller;

import com.nunu.shiro.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tony
 * @since 2024/12/8
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpServletRequest request, HttpServletResponse response) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createResponse("用户名或密码不能为空", false));
        }

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        try {
            subject.login(token);
            // 登录成功后，将用户信息存入 Shiro 的 Session
            subject.getSession().setAttribute("user", userService.getUserByUsername(username));
            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("token", subject.getSession().getId().toString());
            return ResponseEntity.ok(createResponse("登录成功", true, data));
        } catch (UnknownAccountException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createResponse("用户不存在", false));
        } catch (IncorrectCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createResponse("密码错误", false));
        } catch (LockedAccountException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createResponse("账户被锁定", false));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createResponse("认证失败", false));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
            // 清除 Shiro 的 Session 中的用户信息
            subject.getSession().removeAttribute("user");
            return ResponseEntity.ok(createResponse("注销成功", true));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createResponse("未登录", false));
        }
    }

    private Map<String, Object> createResponse(String message, boolean success) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("success", success);
        return response;
    }

    private Map<String, Object> createResponse(String message, boolean success, Map<String, Object> data) {
        Map<String, Object> response = createResponse(message, success);
        response.put("data", data);
        return response;
    }
}