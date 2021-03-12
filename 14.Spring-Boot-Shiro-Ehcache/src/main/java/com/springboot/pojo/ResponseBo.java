package com.springboot.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: ResponseBo
 *
 * @author hbl
 * @date 2021/03/11 0011 10:13
 */
public class ResponseBo extends HashMap<String, Object>
{
    private static final long serialVersionUID = 1836351621449959724L;

    public ResponseBo()
    {
        put("code", 0);
        put("msg", "操作成功");
    }

    public static ResponseBo error()
    {
        return error(1, "操作失败");
    }

    public static ResponseBo error(String msg)
    {
        return error(500, msg);
    }

    private static ResponseBo error(int code, String msg)
    {
        ResponseBo result = new ResponseBo();
        result.put("code", code);
        result.put("msg", msg);
        return result;
    }

    public static ResponseBo ok()
    {
        return new ResponseBo();
    }

    public static ResponseBo ok(String msg)
    {
        ResponseBo result = new ResponseBo();
        result.put("msg", msg);
        return result;
    }

    public static ResponseBo ok(Map<String, Object> map)
    {
        ResponseBo result = new ResponseBo();
        result.putAll(map);
        return result;
    }

    @Override
    public ResponseBo put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }
}
