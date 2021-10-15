package com.springboot.exception;

/**
 * Description: SystemException
 *
 * @author hbl
 * @date 2021/05/28 0028 10:02
 */
public class SystemException extends Exception
{
    private static final long serialVersionUID = 3325760891185185092L;

    public SystemException(String message)
    {
        super(message);
    }
}
