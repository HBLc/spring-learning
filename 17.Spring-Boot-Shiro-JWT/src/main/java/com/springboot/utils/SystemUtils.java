package com.springboot.utils;

import com.springboot.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Description: SystemUtils
 *
 * @author hbl
 * @date 2021/05/26 0026 13:55
 */
@Slf4j
public class SystemUtils
{
    private static List<User> users()
    {
        List<User> users = new ArrayList<>();
        // 模拟两个用户:
        // 1.用户名 admin, 密码 123456, 角色 admin(管理员), 权限"user:add", "user:view"
        // 2.用户名 scott, 密码 123456, 角色 regist(注册用户), 权限"user:view"
        users.add(new User("admin",
                "bfc62b3f67a4c3e57df84dad8cc48a3b",
                new HashSet<>(Collections.singletonList("admin")),
                new HashSet<>(Arrays.asList("user:add", "user:view"))));
        users.add(new User("scott",
                "11bd73355c7bbbac151e4e4f943e59be",
                new HashSet<>(Collections.singletonList("regist")),
                new HashSet<>(Collections.singletonList("user:view"))));
        return users;
    }

    public static User getUser(String username)
    {
        List<User> users = SystemUtils.users();
        return users.stream().filter(user -> StringUtils.equalsIgnoreCase(username, user.getUsername())).findFirst().orElse(null);
    }
}
