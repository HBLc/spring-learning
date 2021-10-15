package com.springboot.handler;

import com.springboot.domain.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthenticatedException;
import org.omg.CORBA.SystemException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;


/**
 * Description: GlobalExceptionHandler
 *
 * @author hbl
 * @date 2021/05/27 0027 16:06
 */
@Slf4j
@RestControllerAdvice
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler
{
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleException(Exception e)
    {
        log.error("系统内部异常, 异常信息: ", e);
        return new Response().message(e.getMessage());
    }

    @ExceptionHandler(value = SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleSystemException(SystemException e)
    {
        log.error("系统错误", e);
        return new Response().message(e.getMessage());
    }

    /**
     * 统一处理请求参数校验（实体对象传参）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response validExceptionHandler(BindException e)
    {
        StringBuilder message = new StringBuilder();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError error : fieldErrors)
        {
            message.append(error.getField()).append(error.getDefaultMessage()).append(",");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        return new Response().message(message.toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e)
    {
        log.error("参数校验异常", e);
        // 获取异常信息
        BindingResult bindingResult = e.getBindingResult();
        // 判断异常中是否有错误信息, 如果存在就使用异常中的消息, 否则使用默认消息
        if (bindingResult.hasErrors())
        {
            List<ObjectError> errors = bindingResult.getAllErrors();
            if (!errors.isEmpty())
            {
                // 这里列出了全部的错误参数, 按正常逻辑, 只需要处理第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return new Response().message(fieldError.getDefaultMessage());
            }
        }
        return new Response().message("参数错误");
    }

    @ExceptionHandler(value = UnauthenticatedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handlerUnauthenticatedException(UnauthenticatedException e)
    {
        log.error("权限不足", e);
    }
}
