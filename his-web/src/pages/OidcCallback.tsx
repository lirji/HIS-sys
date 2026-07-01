import { useEffect } from 'react';
import { Spin, Result, Button } from 'antd';
import { useAuth as useOidc } from 'react-oidc-context';
import { useNavigate } from 'react-router-dom';

/**
 * OIDC 授权码回调页。react-oidc-context 在 AuthProvider 中自动完成 code→token 交换;
 * 完成后本页把用户导回首页。异常则给出重试入口。
 */
export default function OidcCallback() {
  const oidc = useOidc();
  const navigate = useNavigate();

  useEffect(() => {
    if (oidc.isAuthenticated) {
      navigate('/', { replace: true });
    }
  }, [oidc.isAuthenticated, navigate]);

  if (oidc.error) {
    return (
      <Result
        status="error"
        title="登录失败"
        subTitle={oidc.error.message}
        extra={<Button type="primary" onClick={() => oidc.signinRedirect()}>重新登录</Button>}
      />
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '120px 0' }}>
      <Spin size="large" tip="正在完成登录…">
        <div style={{ width: 1, height: 1 }} />
      </Spin>
    </div>
  );
}
