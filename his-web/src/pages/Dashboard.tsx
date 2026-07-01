import { useEffect, useState } from 'react';
import { Card, Col, Row, Steps, Tag, theme, Tooltip } from 'antd';
import {
  ScheduleOutlined,
  TeamOutlined,
  PayCircleOutlined,
  BellOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons';
import type { EChartsOption } from 'echarts';
import { useNavigate } from 'react-router-dom';
import PageContainer from '../components/PageContainer';
import StatCard from '../components/StatCard';
import EChart from '../components/EChart';
import { MENUS } from '../menu';
import { useAuth } from '../store/auth';
import { useThemeStore } from '../store/theme';
import { unreadCount } from '../api/endpoints';

const DEMO = (
  <Tooltip title="演示数据:接入后端聚合接口 /dashboard/stats 后即为真实值">
    <Tag icon={<InfoCircleOutlined />} color="default" style={{ marginInlineStart: 8 }}>
      示例数据
    </Tag>
  </Tooltip>
);

export default function Dashboard() {
  const navigate = useNavigate();
  const { token } = theme.useToken();
  const primary = useThemeStore((s) => s.primary);
  const { user, hasAnyRole } = useAuth();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    unreadCount()
      .then((r) => setUnread(r.count))
      .catch(() => void 0);
  }, []);

  const visible = MENUS.filter((m) => m.path !== '/' && hasAnyRole(m.roles));

  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const trendOption: EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['挂号量', '收入(百元)'], right: 0, textStyle: { color: token.colorTextSecondary } },
    grid: { left: 8, right: 8, top: 40, bottom: 8, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days,
      axisLine: { lineStyle: { color: token.colorBorder } },
      axisLabel: { color: token.colorTextSecondary },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: token.colorBorderSecondary } },
      axisLabel: { color: token.colorTextSecondary },
    },
    series: [
      {
        name: '挂号量',
        type: 'line',
        smooth: true,
        data: [120, 132, 101, 134, 190, 230, 110],
        itemStyle: { color: primary },
        areaStyle: { color: `${primary}33` },
        lineStyle: { width: 3 },
      },
      {
        name: '收入(百元)',
        type: 'line',
        smooth: true,
        data: [88, 95, 70, 112, 150, 198, 90],
        itemStyle: { color: '#13c2c2' },
        areaStyle: { color: '#13c2c233' },
        lineStyle: { width: 3 },
      },
    ],
  };

  const donutOption: EChartsOption = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: token.colorTextSecondary } },
    series: [
      {
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: token.colorBgContainer, borderWidth: 2 },
        label: { show: false },
        data: [
          { value: 38, name: '审方待办' },
          { value: 22, name: '退费审批' },
          { value: 30, name: '收费待结' },
          { value: 18, name: '系统通知' },
        ],
        color: [primary, '#13c2c2', '#722ed1', '#f5701a'],
      },
    ],
  };

  const deptOption: EChartsOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 8, right: 16, top: 16, bottom: 8, containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: token.colorBorderSecondary } },
      axisLabel: { color: token.colorTextSecondary },
    },
    yAxis: {
      type: 'category',
      data: ['口腔科', '骨科', '儿科', '外科', '内科'],
      axisLine: { lineStyle: { color: token.colorBorder } },
      axisLabel: { color: token.colorTextSecondary },
    },
    series: [
      {
        type: 'bar',
        data: [56, 78, 95, 120, 156],
        barWidth: 16,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: `${primary}66` },
              { offset: 1, color: primary },
            ],
          },
        },
      },
    ],
  };

  return (
    <PageContainer
      title={`你好，${user?.realName || user?.username} 👋`}
      subtitle={
        <>
          欢迎回到 HIS 医疗信息平台 · 当前角色
          {user?.roles.map((r) => (
            <Tag key={r} color={primary} style={{ marginInlineStart: 6 }}>
              {r}
            </Tag>
          ))}
        </>
      }
    >
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <StatCard title="今日挂号" value={1086} icon={<ScheduleOutlined />} color={primary} trend={12.5} />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard title="在诊患者" value={342} icon={<TeamOutlined />} color="#13c2c2" trend={3.2} />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard title="今日收入" value={86420} prefix="¥" icon={<PayCircleOutlined />} color="#722ed1" trend={-2.1} />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard title="待办通知" value={unread} suffix="条" icon={<BellOutlined />} color="#f5701a" />
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="近 7 日挂号 / 收入趋势" extra={DEMO} variant="borderless">
            <EChart option={trendOption} height={300} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="待办分布" extra={DEMO} variant="borderless">
            <EChart option={donutOption} height={300} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card title="各科室就诊量排行" extra={DEMO} variant="borderless">
            <EChart option={deptOption} height={260} />
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="门诊业务主线（事件驱动 Saga）" variant="borderless">
            <Steps
              direction="vertical"
              size="small"
              current={6}
              items={[
                { title: '建档', description: '患者主索引 EMPI' },
                { title: '挂号', description: 'Redis 原子锁防超挂' },
                { title: '接诊开方', description: '充血模型 + 状态机' },
                { title: '药师审方', description: 'DRUG 送审' },
                { title: '收费', description: '消费医嘱事件计费' },
                { title: '病历 / FHIR', description: 'HAPI FHIR R4' },
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Card title="快捷入口" variant="borderless">
        <Row gutter={[16, 16]}>
          {visible.map((m) => (
            <Col xs={12} sm={8} lg={6} key={m.path}>
              <Card
                hoverable
                variant="outlined"
                styles={{ body: { padding: 16, display: 'flex', alignItems: 'center', gap: 12 } }}
                onClick={() => navigate(m.path)}
              >
                <span
                  style={{
                    width: 40,
                    height: 40,
                    borderRadius: 10,
                    background: `${primary}1f`,
                    color: primary,
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: 18,
                  }}
                >
                  {m.icon}
                </span>
                <span style={{ fontWeight: 600 }}>{m.label}</span>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>
    </PageContainer>
  );
}
