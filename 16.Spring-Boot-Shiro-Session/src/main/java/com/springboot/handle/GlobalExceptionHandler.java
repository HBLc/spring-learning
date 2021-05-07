package com.springboot.handle;

import org.apache.shiro.authz.AuthorizationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Description: GlobalExceptionHandler
 *
 * @author hbl
 * @date 2021/03/11 0011 18:07
 */
@ControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler
{
    @ExceptionHandler(value = AuthorizationException.class)
    public String handleAuthorizationException()
    {
        return "403";
    }
}
