package com.lrj.his.auth.web;

import java.util.Set;

public record LoginResponse(
        String token,
        long expiresIn,
        Long userId,
        String username,
        String realName,
        Long deptId,
        Set<String> roles) {
}
