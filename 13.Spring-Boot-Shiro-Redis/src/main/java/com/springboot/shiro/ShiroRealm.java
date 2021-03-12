package com.springboot.shiro;

import com.springboot.dao.UserMapper;
import com.springboot.dao.UserPermissionMapper;
import com.springboot.dao.UserRoleMapper;
import com.springboot.pojo.Permission;
import com.springboot.pojo.Role;
import com.springboot.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Description: ShiroRealm
 *
 * @author hbl
 * @date 2021/03/11 0011 14:01
 */
public class ShiroRealm extends AuthorizingRealm
{
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private UserPermissionMapper userPermissionMapper;

    /**
     * 获取用户角色和权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        String userName = user.getUserName();

        System.out.println("用户" + userName + "获取权限------ShiroRealm.doGetAuthorizationInfo");
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        // 获取用户角色集
        List<Role> roleList = userRoleMapper.findByUserName(userName);
        Set<String> roleSet = new HashSet<>();
        for (Role role : roleList)
        {
            roleSet.add(role.getName());
        }
        simpleAuthorizationInfo.setRoles(roleSet);

        // 获取用户权限集
        List<Permission> permissionList = userPermissionMapper.findByUserName(userName);
        Set<String> permissionSet = new HashSet<>();
        for (Permission permission : permissionList)
        {
            permissionSet.add(permission.getName());
        }
        simpleAuthorizationInfo.setStringPermissions(permissionSet);

        return simpleAuthorizationInfo;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException
    {
        String userName = (String) token.getPrincipal();
        String password = new String((char[]) token.getCredentials());

        System.out.println("用户" + userName + "认证--------ShiroRealm.doGetAuthenticationInfo");
        User user = userMapper.findByUserName(userName);

        if (Objects.isNull(user))
        {
            throw new UnknownAccountException("用户名或密码错误！");
        }
        if (!password.equals(user.getPassword()))
        {
            throw new UnknownAccountException("用户名或密码错误！");
        }
        if (user.getStatus().equals("0"))
        {
            throw new UnknownAccountException("帐号已被锁定,请联系管理员！");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, password, getName());
        return info;
    }
}
