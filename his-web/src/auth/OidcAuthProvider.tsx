import { AuthProvider } from 'react-oidc-context';
import { oidcConfig } from './config';
import AuthBridge from './AuthBridge';

/**
 * idp 模式下的顶层包裹:提供 OIDC 上下文并挂载 AuthBridge 把登录态同步进应用 store。
 * password 模式不使用本组件(main.tsx 按 AUTH_MODE 条件挂载)。
 */
export default function OidcAuthProvider({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider {...oidcConfig}>
      <AuthBridge />
      {children}
    </AuthProvider>
  );
}
