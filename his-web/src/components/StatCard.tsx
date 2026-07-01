import { useEffect, useRef, useState } from 'react';
import { Card, Typography, theme } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import type { ReactNode } from 'react';

// 数字滚动:挂载时从 0 缓动到目标值。
function useCountUp(target: number, duration = 900) {
  const [val, setVal] = useState(0);
  const raf = useRef<number>();
  useEffect(() => {
    const start = performance.now();
    const tick = (now: number) => {
      const p = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - p, 3);
      setVal(target * eased);
      if (p < 1) raf.current = requestAnimationFrame(tick);
    };
    raf.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf.current!);
  }, [target, duration]);
  return val;
}

interface Props {
  title: string;
  value: number;
  precision?: number;
  prefix?: string;
  suffix?: string;
  icon: ReactNode;
  color: string;
  trend?: number; // 同比百分比,正负决定上下箭头
}

export default function StatCard({
  title,
  value,
  precision = 0,
  prefix,
  suffix,
  icon,
  color,
  trend,
}: Props) {
  const { token } = theme.useToken();
  const v = useCountUp(value);

  return (
    <Card className="his-pop-in" styles={{ body: { padding: 18 } }} variant="borderless">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <Typography.Text type="secondary" style={{ fontSize: 13 }}>
            {title}
          </Typography.Text>
          <div style={{ fontSize: 28, fontWeight: 700, lineHeight: 1.3, marginTop: 4 }}>
            {prefix}
            {v.toLocaleString('zh-CN', {
              minimumFractionDigits: precision,
              maximumFractionDigits: precision,
            })}
            {suffix && <span style={{ fontSize: 14, fontWeight: 500 }}> {suffix}</span>}
          </div>
        </div>
        <div
          style={{
            width: 52,
            height: 52,
            borderRadius: 14,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 24,
            color: '#fff',
            background: `linear-gradient(135deg, ${color}, ${color}aa)`,
            boxShadow: `0 8px 18px ${color}40`,
          }}
        >
          {icon}
        </div>
      </div>
      {trend !== undefined && (
        <div style={{ marginTop: 10, fontSize: 12 }}>
          <span style={{ color: trend >= 0 ? token.colorSuccess : token.colorError }}>
            {trend >= 0 ? <ArrowUpOutlined /> : <ArrowDownOutlined />} {Math.abs(trend)}%
          </span>
          <Typography.Text type="secondary" style={{ marginInlineStart: 6 }}>
            较昨日
          </Typography.Text>
        </div>
      )}
    </Card>
  );
}
