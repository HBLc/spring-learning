package com.springboot.authentication;

import com.springboot.domain.User;
import com.springboot.utils.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Description: ShiroRealm
 *
 * @author hbl
 * @date 2021/05/28 0028 10:15
 */
public class ShiroRealm extends AuthorizingRealm
{
    /**
     * 必须重写此方法，不然会报错
     */
    @Override
    public boolean supports(AuthenticationToken token)
    {
        return token instanceof JWTToken;
    }

    /**
     * 授权模块
     * @param token toekn
     * @return 权限信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection token)
    {
        // 使用 token 获得 username, 从数据库中查到该用户所拥有的角色和权限存入 SimpleAuthorizationInfo 中
        String username = JWTUtil.getUsername(token.toString());
        User user = SystemUtils.getUser(username);

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        // 获取用户角色集合（模拟值，实际从数据库中取得）
        simpleAuthorizationInfo.setRoles(user.getRole());

        // 获取用户权限集合（模拟值，实际从数据库中取得）
        simpleAuthorizationInfo.setStringPermissions(user.getPermission());

        return simpleAuthorizationInfo;
    }

    /**
     * 用户认证
     * @param token 身份认证
     * @return 身份认证信息
     * @throws AuthenticationException 认证异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
    {
        // 这里的 token 是从 JWTFilter 的 executeLogin 方法传递过来的，已经经过了解密
        String tokenStr = (String) token.getCredentials();

        String username = JWTUtil.getUsername(tokenStr);

        if (StringUtils.isBlank(username))
        {
            throw new AuthenticationException("token校验不通过");
        }

        // 通过用户名查询用户信息
        User user = SystemUtils.getUser(username);

        if (user == null)
        {
            throw new AuthenticationException("用户名或密码错误");
        }
        if (!JWTUtil.verify(tokenStr, username, user.getPassword()))
        {
            throw new AuthenticationException("token校验不通过");
        }

        return new SimpleAuthenticationInfo(token, token, "shiro_realm");
    }
}
