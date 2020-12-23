package com.springboot.shiro;

import com.springboot.dao.UserMapper;
import com.springboot.pojo.User;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: ShiroRealm
 *
 * @author hbl
 * @date 2020/12/23 0023 15:08
 */
public class ShiroRealm extends AuthorizingRealm
{
    @Autowired
    public UserMapper userMapper;

    /**
     * 获取用户角色和权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection)
    {
        return null;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException
    {
        String userName = (String) authenticationToken.getPrincipal();
        String password = new String((char[]) authenticationToken.getCredentials());

        System.out.println("用户" + userName + "认证~~~~~~~~~~~");
        User user = userMapper.findByUserName(userName);

        if (user == null)
        {
            throw new UnknownAccountException("用户名或密码错误");
        }
        if (!password.equals(user.getPassword()))
        {
            throw new IncorrectCredentialsException("用户名或密码错误");
        }
        if (user.getStatus().equals("0"))
        {
            throw new LockedAccountException("账号已被锁定，请联系管理员~");
        }

        return new SimpleAuthenticationInfo(user, password, getName());
    }
}
