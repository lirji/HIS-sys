import { Typography } from 'antd';
import type { ReactNode } from 'react';

// 统一页头 + 内容容器。所有业务页套这个,保证标题/间距/留白一致。
interface Props {
  title: ReactNode;
  subtitle?: ReactNode;
  extra?: ReactNode;
  children: ReactNode;
}

export default function PageContainer({ title, subtitle, extra, children }: Props) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          justifyContent: 'space-between',
          gap: 16,
          flexWrap: 'wrap',
        }}
      >
        <div>
          <Typography.Title level={4} style={{ margin: 0 }}>
            {title}
          </Typography.Title>
          {subtitle && (
            <Typography.Text type="secondary" style={{ fontSize: 13 }}>
              {subtitle}
            </Typography.Text>
          )}
        </div>
        {extra && <div>{extra}</div>}
      </div>
      {children}
    </div>
  );
}
