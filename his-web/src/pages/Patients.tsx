import { useState } from 'react';
import { Card, Form, Input, Button, DatePicker, Descriptions, InputNumber, Space, App, Tag, Select, Row, Col, Empty } from 'antd';
import { SearchOutlined, UserAddOutlined } from '@ant-design/icons';
import { createPatient, getPatient } from '../api/endpoints';
import type { Patient } from '../api/types';
import PageContainer from '../components/PageContainer';

export default function Patients() {
  const { message } = App.useApp();
  const [createForm] = Form.useForm();
  const [created, setCreated] = useState<Patient | null>(null);
  const [queryId, setQueryId] = useState<number | null>(null);
  const [found, setFound] = useState<Patient | null>(null);
  const [loading, setLoading] = useState(false);

  const onCreate = async (v: any) => {
    setLoading(true);
    try {
      const p = await createPatient({
        name: v.name,
        gender: v.gender,
        idCard: v.idCard,
        phone: v.phone,
        address: v.address,
        birthDate: v.birthDate ? v.birthDate.format('YYYY-MM-DD') : undefined,
      });
      setCreated(p);
      message.success(`建档成功,EMPI=${p.empiNo}`);
      createForm.resetFields();
    } catch {
      /* handled */
    } finally {
      setLoading(false);
    }
  };

  const onQuery = async () => {
    if (!queryId) return;
    try {
      setFound(await getPatient(queryId));
    } catch {
      setFound(null);
    }
  };

  const renderPatient = (p: Patient) => (
    <Descriptions bordered size="small" column={2}>
      <Descriptions.Item label="ID">{p.id}</Descriptions.Item>
      <Descriptions.Item label="EMPI">
        <Tag color="geekblue">{p.empiNo}</Tag>
      </Descriptions.Item>
      <Descriptions.Item label="姓名">{p.name}</Descriptions.Item>
      <Descriptions.Item label="性别">
        {p.gender === 'MALE' ? '男' : p.gender === 'FEMALE' ? '女' : '未知'}
      </Descriptions.Item>
      <Descriptions.Item label="出生日期">{p.birthDate || '—'}</Descriptions.Item>
      <Descriptions.Item label="身份证">{p.idCard}</Descriptions.Item>
      <Descriptions.Item label="手机" span={2}>{p.phone || '—'}</Descriptions.Item>
    </Descriptions>
  );

  return (
    <PageContainer title="患者主索引" subtitle="EMPI 患者建档与检索">
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title={<><UserAddOutlined /> 患者建档</>} variant="borderless">
            <Form form={createForm} layout="vertical" onFinish={onCreate}>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
                    <Input placeholder="患者姓名" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="gender" label="性别" rules={[{ required: true, message: '请选择性别' }]}>
                    <Select
                      placeholder="请选择"
                      options={[
                        { value: 'MALE', label: '男' },
                        { value: 'FEMALE', label: '女' },
                        { value: 'UNKNOWN', label: '未知' },
                      ]}
                    />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="idCard" label="身份证号" rules={[{ required: true }]}>
                <Input placeholder="18 位身份证号" />
              </Form.Item>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="birthDate" label="出生日期">
                    <DatePicker style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="phone" label="手机号">
                    <Input placeholder="选填" />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="address" label="住址">
                <Input placeholder="选填" />
              </Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} icon={<UserAddOutlined />}>
                建档
              </Button>
            </Form>
            {created && (
              <div style={{ marginTop: 16 }}>{renderPatient(created)}</div>
            )}
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title={<><SearchOutlined /> 按 ID 查询患者</>} variant="borderless">
            <Space.Compact style={{ width: '100%' }}>
              <InputNumber
                placeholder="输入患者 ID"
                value={queryId ?? undefined}
                onChange={(v) => setQueryId(v)}
                onPressEnter={onQuery}
                style={{ flex: 1 }}
              />
              <Button type="primary" icon={<SearchOutlined />} onClick={onQuery}>
                查询
              </Button>
            </Space.Compact>
            <div style={{ marginTop: 16 }}>
              {found ? renderPatient(found) : <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="输入 ID 查询患者档案" />}
            </div>
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
}
