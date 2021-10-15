package com.springboot.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.springboot.properties.SystemProperties;
import com.springboot.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Description: JWTUtil
 *
 * @author hbl
 * @date 2021/05/28 0028 10:04
 */
@Slf4j
public class JWTUtil
{
    private static final long EXPIRE_TIME = SpringContextUtil.getBean(SystemProperties.class).getJwtTimeOut() * 1000;

    /**
     * 校验 token 是否正确
     */
    public static boolean verify(String token, String username, String secret)
    {
        try
        {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
            verifier.verify(token);
            log.info("token 有效~");
            return true;
        }
        catch (Exception e)
        {
            log.info("token 无效: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 token 中获取用户名
     */
    public static String getUsername(String token)
    {
        try
        {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        }
        catch (JWTDecodeException e)
        {
            log.error("token 无效: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成 token
     */
    public static String sign(String username, String secret)
    {
        try
        {
            username = StringUtils.lowerCase(username);
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);
        }
        catch (Exception e)
        {
            log.error("错误: {}", e.getMessage());
            return null;
        }
    }
}
