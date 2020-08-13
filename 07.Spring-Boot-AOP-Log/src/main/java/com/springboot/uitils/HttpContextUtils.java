package com.springboot.uitils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Description: HttpContextUtils
 *
 * @author hbl
 * @date 2020/8/13 0013 15:03
 */
public class HttpContextUtils
{
    public static HttpServletRequest getHttpServletRequest()
    {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }
}
