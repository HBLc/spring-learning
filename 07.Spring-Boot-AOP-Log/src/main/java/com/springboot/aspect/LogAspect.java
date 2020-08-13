package com.springboot.aspect;

import com.springboot.annotation.Log;
import com.springboot.dao.SysLogDao;
import com.springboot.domain.SysLog;
import com.springboot.uitils.HttpContextUtils;
import com.springboot.uitils.IPUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * Description: LogAspect
 *
 * @author hbl
 * @date 2020/8/13 0013 14:40
 */
@Aspect
@Component
public class LogAspect
{
    private final SysLogDao sysLogDao;

    public LogAspect(SysLogDao sysLogDao)
    {
        this.sysLogDao = sysLogDao;
    }

    @Pointcut("@annotation(com.springboot.annotation.Log)")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point)
    {
        Object result = null;
        long beginTime = System.currentTimeMillis();
        try
        {
            // 执行方法
            result = point.proceed();
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
        // 执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        // 保存日志
        saveLog(point, time);
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, long time)
    {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        SysLog sysLog = new SysLog();
        Log logAnnotation = method.getAnnotation(Log.class);
        if (logAnnotation != null)
        {
            // 注解上的描述
            sysLog.setOperation(logAnnotation.value());
        }
        // 请求的方法名
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");
        // 请求的方法参数值
        Object[] args = point.getArgs();
        // 请求的方法参数名称
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null)
        {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < args.length; i++)
            {
                params.append(" ").append(paramNames[i]).append(": ").append(args[i]);
            }
            sysLog.setParams(params.toString());
        }
        // 获取 request
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        // 设置 IP 地址
        sysLog.setIp(IPUtils.getIpAddr(request));
        // 模拟一个用户名 admin
        sysLog.setUsername("admin");
        sysLog.setTime((int) time);
        sysLog.setCreateTime(new Date());
        // 保存系统日志
        sysLogDao.saveSysLog(sysLog);
    }
}
