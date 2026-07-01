import { IS_IDP } from '../auth/config';
import PasswordLogin from './PasswordLogin';
import SsoLogin from './SsoLogin';

// 登录页按鉴权模式分流:idp → OIDC SSO;password(默认)→ 账号密码表单。
// AUTH_MODE 为构建期常量,分支稳定,不违反 hooks 规则。
export default function Login() {
  return IS_IDP ? <SsoLogin /> : <PasswordLogin />;
}
