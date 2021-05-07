package com.springboot.service;

import com.springboot.pojo.UserOnline;

import java.util.List;

/**
 * Description: SessionServiceImpl
 *
 * @author hbl
 * @date 2021/04/30 0030 14:07
 */
public interface SessionService
{
    List<UserOnline> list();

    boolean forceLogout(String sessionId);
}
