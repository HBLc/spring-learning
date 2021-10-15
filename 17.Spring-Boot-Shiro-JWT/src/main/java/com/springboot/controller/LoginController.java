package com.springboot.controller;

import com.springboot.authentication.JWTUtil;
import com.springboot.domain.Response;
import com.springboot.domain.User;
import com.springboot.exception.SystemException;
import com.springboot.properties.SystemProperties;
import com.springboot.utils.MD5Util;
import com.springboot.utils.SystemUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: LoginController
 *
 * @author hbl
 * @date 2021/05/28 0028 11:33
 */
@RestController
@Validated
@RequiredArgsConstructor
public class LoginController
{
    private final SystemProperties systemProperties;

    @PostMapping("/login")
    public Response login(@NotBlank(message = "{required}") String username,
                          @NotBlank(message = "{required}") String password) throws SystemException
    {
        username = StringUtils.lowerCase(username);
        password = MD5Util.encrypt(username, password);

        final String errorMsg = "用户名或密码错误";
        User user = SystemUtils.getUser(username);

        if (user == null)
        {
            throw new SystemException(errorMsg);
        }
        if (!StringUtils.equals(user.getPassword(), password))
        {
            throw new SystemException(errorMsg);
        }

        // 生成 Token
        String token = JWTUtil.sign(username, password);

        Map<String, Object> userInfo = this.generateUserInfo(token, user);
        return new Response().message("认证成功").data(userInfo);
    }

    /**
     * 生成前端需要的用户信息
     */
    private Map<String, Object> generateUserInfo(String token, User user)
    {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token);

        user.setPassword("");
        userInfo.put("user", user);
        return userInfo;
    }
}
