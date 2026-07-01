import type { AuthProviderProps } from 'react-oidc-context';
import { UserManager, WebStorageStateStore, type UserManagerSettings } from 'oidc-client-ts';
import type { LoginResponse } from '../api/types';

// 鉴权模式:password(默认,账号密码 + HS256)/ idp(纯 SSO,OIDC 授权码+PKCE)/
// dual(两者共存:一页两入口,网关双令牌校验)。由构建期 VITE_AUTH_MODE 决定,默认 password。
export const AUTH_MODE = (import.meta.env.VITE_AUTH_MODE ?? 'password') as 'password' | 'idp' | 'dual';
export const IS_IDP = AUTH_MODE === 'idp';
export const IS_DUAL = AUTH_MODE === 'dual';
// 需要挂载 OIDC 上下文(SSO 入口)的模式:idp 与 dual
export const OIDC_ENABLED = IS_IDP || IS_DUAL;

// 登录方式标记(用于登出分流:sso→IdP 端登出;password→本地清理)
const AUTH_KIND_KEY = 'his_auth_kind';
export function setAuthKind(kind: 'password' | 'sso') {
  localStorage.setItem(AUTH_KIND_KEY, kind);
}
export function getAuthKind(): 'password' | 'sso' | null {
  return localStorage.getItem(AUTH_KIND_KEY) as 'password' | 'sso' | null;
}

// authority 必须是浏览器可达的 his-idp issuer(默认 dev 直连 9008)。
const AUTHORITY = import.meta.env.VITE_OIDC_AUTHORITY ?? 'http://localhost:9008';
const CLIENT_ID = import.meta.env.VITE_OIDC_CLIENT_ID ?? 'his-web';

const settings: UserManagerSettings = {
  authority: AUTHORITY,
  client_id: CLIENT_ID,
  redirect_uri: `${window.location.origin}/oidc-callback`,
  post_logout_redirect_uri: `${window.location.origin}/login`,
  response_type: 'code',
  scope: 'openid profile',
  // 登录态存 localStorage,刷新页面不丢失
  userStore: new WebStorageStateStore({ store: window.localStorage }),
};

export const oidcConfig: AuthProviderProps = {
  ...settings,
  automaticSilentRenew: true,
  // 授权码换 token 后清掉 URL 上的 code/state
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, '/');
  },
};

// 独立 UserManager,供命令式登出(退出登录按钮)复用同一 localStorage 会话;
// 与 AuthProvider 内部 manager 共享存储,读取 id_token 作 end_session 提示。
export const userManager: UserManager | null = OIDC_ENABLED ? new UserManager(settings) : null;

/** 从 OIDC id_token claims(profile)映射为应用内的用户会话结构。 */
export function profileToSession(token: string, profile: Record<string, unknown>): LoginResponse {
  const roles = Array.isArray(profile.roles) ? (profile.roles as string[]) : [];
  return {
    token,
    expiresIn: 0,
    userId: Number(profile.uid ?? 0),
    username: String(profile.preferred_username ?? profile.sub ?? ''),
    realName: String(profile.realName ?? profile.preferred_username ?? ''),
    deptId: profile.dept != null ? Number(profile.dept) : null,
    roles,
  };
}
