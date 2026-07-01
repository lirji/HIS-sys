/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 鉴权模式:password(默认)| idp(OIDC SSO) */
  readonly VITE_AUTH_MODE?: 'password' | 'idp';
  /** idp 模式:his-idp issuer(浏览器可达),默认 http://localhost:9008 */
  readonly VITE_OIDC_AUTHORITY?: string;
  /** idp 模式:OIDC client_id,默认 his-web */
  readonly VITE_OIDC_CLIENT_ID?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
