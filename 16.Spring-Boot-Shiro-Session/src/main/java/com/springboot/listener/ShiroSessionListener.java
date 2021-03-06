package com.springboot.listener;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: ShiroSessionListener
 *
 * @author hbl
 * @date 2021/04/30 0030 13:54
 */
public class ShiroSessionListener implements SessionListener
{
    private final AtomicInteger sessionCount = new AtomicInteger(0);

    @Override
    public void onStart(Session session)
    {
        sessionCount.incrementAndGet();
    }

    @Override
    public void onStop(Session session)
    {
        sessionCount.decrementAndGet();
    }

    @Override
    public void onExpiration(Session session)
    {
        sessionCount.decrementAndGet();
    }
}
