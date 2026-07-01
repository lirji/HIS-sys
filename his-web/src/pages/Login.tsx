import { AUTH_MODE } from '../auth/config';
import PasswordLogin from './PasswordLogin';
import SsoLogin from './SsoLogin';
import DualLogin from './DualLogin';

// 登录页按鉴权模式分流:idp → 纯 SSO;dual → 账号密码 + SSO 一页两入口;
// password(默认)→ 账号密码表单。AUTH_MODE 为构建期常量,分支稳定,不违反 hooks 规则。
export default function Login() {
  if (AUTH_MODE === 'idp') return <SsoLogin />;
  if (AUTH_MODE === 'dual') return <DualLogin />;
  return <PasswordLogin />;
}
