package com.springboot.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * Description: JWTToken
 *
 * @author hbl
 * @date 2021/05/28 0028 10:37
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTToken implements AuthenticationToken
{
    private static final long serialVersionUID = 6643623118137694691L;

    private String token;

    private String expireAt;

    @Override
    public Object getPrincipal()
    {
        return token;
    }

    @Override
    public Object getCredentials()
    {
        return token;
    }

    public JWTToken(String token)
    {
        this.token = token;
    }
}
