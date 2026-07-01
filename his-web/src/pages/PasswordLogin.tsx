import { useState } from 'react';
import { Form, Input, Button, Typography, Tag, Space, App, theme } from 'antd';
import { UserOutlined, LockOutlined, MedicineBoxFilled, SafetyCertificateOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/endpoints';
import { useAuth } from '../store/auth';
import { useThemeStore } from '../store/theme';

const DEMO_ACCOUNTS = ['admin', 'doctor', 'nurse', 'cashier', 'pharmacist'];

export default function PasswordLogin() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const { token } = theme.useToken();
  const primary = useThemeStore((s) => s.primary);
  const setSession = useAuth((s) => s.setSession);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onFinish = async (v: { username: string; password: string }) => {
    setLoading(true);
    try {
      const resp = await login(v.username, v.password);
      setSession(resp);
      message.success(`欢迎，${resp.realName || resp.username}`);
      navigate('/');
    } catch {
      /* 拦截器已提示 */
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', background: token.colorBgLayout }}>
      {/* 左侧品牌区:渐变 + 介绍,窄屏隐藏 */}
      <div
        className="his-login-brand"
        style={{
          flex: 1,
          background: `linear-gradient(135deg, ${primary} 0%, #722ed1 100%)`,
          color: '#fff',
          padding: '64px 56px',
          flexDirection: 'column',
          justifyContent: 'center',
          gap: 24,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, fontSize: 30, fontWeight: 800 }}>
          <span
            style={{
              width: 56,
              height: 56,
              borderRadius: 16,
              background: 'rgba(255,255,255,0.18)',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <MedicineBoxFilled />
          </span>
          HIS 医疗信息平台
        </div>
        <Typography.Title level={2} style={{ color: '#fff', margin: 0, maxWidth: 460 }}>
          DDD 领域建模 · Spring Cloud 微服务 · FHIR R4
        </Typography.Title>
        <Typography.Paragraph style={{ color: 'rgba(255,255,255,0.85)', fontSize: 16, maxWidth: 460 }}>
          覆盖建档、挂号、门诊医嘱、药师审方、收费结算、退费审批、病历与通知中心的一体化门诊业务平台。
        </Typography.Paragraph>
        <Space size={20} style={{ color: 'rgba(255,255,255,0.9)' }}>
          <span><SafetyCertificateOutlined /> JWT + RBAC</span>
          <span>事件驱动 Saga</span>
          <span>可观测治理</span>
        </Space>
        <div
          style={{
            position: 'absolute',
            right: -80,
            bottom: -80,
            width: 280,
            height: 280,
            borderRadius: '50%',
            background: 'rgba(255,255,255,0.08)',
          }}
        />
      </div>

      {/* 右侧表单区 */}
      <div
        style={{
          width: 460,
          maxWidth: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 32,
        }}
      >
        <div style={{ width: '100%', maxWidth: 340 }}>
          <Typography.Title level={3} style={{ marginBottom: 4 }}>
            欢迎登录
          </Typography.Title>
          <Typography.Text type="secondary">请输入账号信息进入工作台</Typography.Text>
          <Form form={form} onFinish={onFinish} size="large" style={{ marginTop: 28 }}>
            <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input prefix={<UserOutlined />} placeholder="用户名" />
            </Form.Item>
            <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password prefix={<LockOutlined />} placeholder="密码" />
            </Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              登 录
            </Button>
          </Form>
          <div style={{ marginTop: 20 }}>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              演示账号（密码 123456，点击填入）：
            </Typography.Text>
            <Space wrap style={{ marginTop: 10 }}>
              {DEMO_ACCOUNTS.map((u) => (
                <Tag
                  key={u}
                  color={primary}
                  style={{ cursor: 'pointer' }}
                  onClick={() => form.setFieldsValue({ username: u, password: '123456' })}
                >
                  {u}
                </Tag>
              ))}
            </Space>
          </div>
        </div>
      </div>
    </div>
  );
}
