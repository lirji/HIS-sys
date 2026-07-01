import { useState } from 'react';
import { Card, InputNumber, Button, Space, App, Typography, Empty } from 'antd';
import { FileTextOutlined } from '@ant-design/icons';
import { generateEmr, getEmrDocument } from '../api/endpoints';
import { useAuth } from '../store/auth';
import PageContainer from '../components/PageContainer';

export default function Emr() {
  const { message } = App.useApp();
  const isDoctor = useAuth((s) => s.hasAnyRole(['DOCTOR']));
  const [encounterId, setEncounterId] = useState<number | null>(null);
  const [bundle, setBundle] = useState<string>('');
  const [loading, setLoading] = useState(false);

  const onGenerate = async () => {
    if (!encounterId) return;
    const docId = await generateEmr(encounterId);
    message.success(`病历文档已生成 / 刷新,docId=${docId}`);
  };

  const onView = async () => {
    if (!encounterId) return;
    setLoading(true);
    try {
      const raw = await getEmrDocument(encounterId);
      try {
        setBundle(JSON.stringify(JSON.parse(raw), null, 2));
      } catch {
        setBundle(raw);
      }
    } catch {
      setBundle('');
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageContainer title="病历 / FHIR" subtitle="查看 HAPI FHIR R4 输出的病历 Bundle">
      <Card title={<><FileTextOutlined /> 病历文档 / FHIR R4 Bundle</>} variant="borderless">
        <Space wrap>
          就诊号：
          <InputNumber value={encounterId ?? undefined} onChange={(v) => setEncounterId(v)} onPressEnter={onView} placeholder="encounterId" />
          {isDoctor && <Button onClick={onGenerate}>生成 / 刷新病历</Button>}
          <Button type="primary" loading={loading} onClick={onView}>
            查看 FHIR Bundle
          </Button>
        </Space>
        <Typography.Paragraph type="secondary" style={{ marginTop: 12, marginBottom: 0 }}>
          病历在缴费闭环时经 Kafka 自动生成;此处可手动触发并查看 HAPI FHIR 输出的 Bundle JSON。
        </Typography.Paragraph>
      </Card>

      <Card title="FHIR Bundle (application/fhir+json)" variant="borderless">
        {bundle ? (
          <pre
            style={{
              maxHeight: 560,
              overflow: 'auto',
              background: '#0d1117',
              color: '#c9d1d9',
              padding: 16,
              borderRadius: 8,
              fontSize: 12,
              lineHeight: 1.6,
              margin: 0,
            }}
          >
            {bundle}
          </pre>
        ) : (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="查询就诊号以查看病历 Bundle" />
        )}
      </Card>
    </PageContainer>
  );
}
