import { useEffect } from 'react';
import { Button, Typography, Space, theme } from 'antd';
import { MedicineBoxFilled, SafetyCertificateOutlined, LoginOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth as useOidc } from 'react-oidc-context';
import { useThemeStore } from '../store/theme';

/**
 * idp 模式登录页。经 his-idp 走 OIDC 授权码 + PKCE:点击后 signinRedirect 跳转统一身份认证,
 * 认证成功回到 /oidc-callback 换 token 再进工作台。
 */
export default function SsoLogin() {
  const oidc = useOidc();
  const navigate = useNavigate();
  const { token } = theme.useToken();
  const primary = useThemeStore((s) => s.primary);

  useEffect(() => {
    if (oidc.isAuthenticated) {
      navigate('/', { replace: true });
    }
  }, [oidc.isAuthenticated, navigate]);

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: `linear-gradient(135deg, ${primary} 0%, #722ed1 100%)`,
      }}
    >
      <div
        style={{
          width: 380,
          maxWidth: '92%',
          background: token.colorBgContainer,
          borderRadius: 16,
          padding: '40px 36px',
          boxShadow: '0 12px 40px rgba(0,0,0,0.18)',
          textAlign: 'center',
        }}
      >
        <span
          style={{
            width: 64,
            height: 64,
            borderRadius: 18,
            background: primary,
            color: '#fff',
            fontSize: 30,
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: 16,
          }}
        >
          <MedicineBoxFilled />
        </span>
        <Typography.Title level={3} style={{ marginBottom: 4 }}>
          HIS 医疗信息平台
        </Typography.Title>
        <Typography.Paragraph type="secondary" style={{ marginBottom: 28 }}>
          <SafetyCertificateOutlined /> 统一身份认证(SSO)· OIDC 授权码 + PKCE
        </Typography.Paragraph>
        <Button
          type="primary"
          size="large"
          block
          icon={<LoginOutlined />}
          loading={oidc.isLoading}
          onClick={() => oidc.signinRedirect()}
        >
          使用统一身份登录
        </Button>
        <div style={{ marginTop: 16 }}>
          <Space size={16} style={{ color: token.colorTextSecondary, fontSize: 12 }}>
            <span>RS256 · JWK</span>
            <span>网关 Resource Server</span>
          </Space>
        </div>
        {oidc.error && (
          <Typography.Paragraph type="danger" style={{ marginTop: 16, marginBottom: 0 }}>
            登录出错:{oidc.error.message}
          </Typography.Paragraph>
        )}
      </div>
    </div>
  );
}
