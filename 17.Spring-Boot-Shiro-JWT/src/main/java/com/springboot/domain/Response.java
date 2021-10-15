package com.springboot.domain;

import java.util.HashMap;

/**
 * Description: Response
 *
 * @author hbl
 * @date 2021/05/11 0011 17:57
 */
public class Response extends HashMap<String, Object>
{
    private static final long serialVersionUID = 2312999149351397309L;

    public Response message(String message)
    {
        this.put("message", message);
        return this;
    }

    public Response data(Object data)
    {
        this.put("data", data);
        return this;
    }

    @Override
    public Response put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
