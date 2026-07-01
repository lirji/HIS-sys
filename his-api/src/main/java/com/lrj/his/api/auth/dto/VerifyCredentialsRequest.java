package com.lrj.his.api.auth.dto;

import java.io.Serializable;

/**
 * IdP → his-auth 的口令校验请求。his-idp 授权码登录时用它把用户名/口令交给 his-auth 核验,
 * 避免在 IdP 侧再存一份口令散列(用户/口令仍以 his-auth 为单一事实来源)。
 */
public record VerifyCredentialsRequest(String username, String password) implements Serializable {
}
