import type { AuthProviderProps } from 'react-oidc-context';
import { UserManager, WebStorageStateStore, type UserManagerSettings } from 'oidc-client-ts';
import type { LoginResponse } from '../api/types';

// 鉴权模式:password(默认,his-auth 账号密码 + HS256)/ idp(his-idp OIDC 授权码+PKCE)。
// 由构建期环境变量 VITE_AUTH_MODE 决定,默认 password 保持既有主线不变。
export const AUTH_MODE = (import.meta.env.VITE_AUTH_MODE ?? 'password') as 'password' | 'idp';
export const IS_IDP = AUTH_MODE === 'idp';

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
export const userManager: UserManager | null = IS_IDP ? new UserManager(settings) : null;

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
