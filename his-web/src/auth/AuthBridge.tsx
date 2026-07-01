import { useEffect } from 'react';
import { useAuth as useOidc } from 'react-oidc-context';
import { useAuth } from '../store/auth';
import { profileToSession, setAuthKind } from './config';

/**
 * OIDC 态 → 应用态桥接。把 react-oidc-context 的登录结果同步进 zustand store
 * (token=access_token 供网关校验;用户信息取自 id_token claims),
 * 使 ProtectedRoute / axios 拦截器 / 菜单过滤等既有逻辑零改动继续工作。
 */
export default function AuthBridge() {
  const oidc = useOidc();
  const setSession = useAuth((s) => s.setSession);
  const clearSession = useAuth((s) => s.clearSession);

  useEffect(() => {
    if (oidc.isAuthenticated && oidc.user) {
      const accessToken = oidc.user.access_token;
      const profile = oidc.user.profile as Record<string, unknown>;
      setAuthKind('sso');
      setSession(profileToSession(accessToken, profile));
    } else if (!oidc.isLoading && !oidc.isAuthenticated) {
      // 用 clearSession 而非 logout:避免 localStorage.clear() 抹掉 oidc-client 自身登录态
      clearSession();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [oidc.isAuthenticated, oidc.isLoading, oidc.user]);

  return null;
}
