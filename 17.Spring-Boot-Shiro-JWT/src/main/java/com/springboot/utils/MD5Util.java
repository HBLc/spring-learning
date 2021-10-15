package com.springboot.utils;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

/**
 * Description: MD5Util
 *
 * @author hbl
 * @date 2021/05/27 0027 09:43
 */
public class MD5Util
{
    private static final String ALGORITH_NAME = "MD5";
    private static final int HASH_ITREATIONS = 2;

    public static String encrypt(String password)
    {
        return new SimpleHash(ALGORITH_NAME, password, ByteSource.Util.bytes(password), HASH_ITREATIONS).toHex();
    }

    public static String encrypt(String username, String password)
    {
        return new SimpleHash(ALGORITH_NAME, password, ByteSource.Util.bytes(username.toLowerCase() + password), HASH_ITREATIONS).toHex();
    }

    public static void main(String[] args)
    {
        System.out.println(encrypt("admin", "123456"));
    }
}
