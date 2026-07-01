import { useState } from 'react';
import {
  Card, Form, Input, Button, InputNumber, Select, Table, Space, App, Tag, Descriptions, Divider, Popconfirm, Row, Col,
} from 'antd';
import {
  openEncounter, recordDiagnosis, addOrder, listOrders, submitOrders, cancelOrder, resubmitOrder,
} from '../api/endpoints';
import type { Encounter, Order } from '../api/types';
import PageContainer from '../components/PageContainer';
import StatusTag from '../components/StatusTag';

const ORDER_TYPES = [
  { value: 'DRUG', label: '药品(需药师审方)' },
  { value: 'EXAM', label: '检查' },
  { value: 'LAB', label: '检验' },
  { value: 'TREATMENT', label: '治疗' },
];

export default function Outpatient() {
  const { message } = App.useApp();
  const [encounter, setEncounter] = useState<Encounter | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [apptId, setApptId] = useState<number | null>(null);
  const [diagForm] = Form.useForm();
  const [orderForm] = Form.useForm();

  const refreshOrders = async (id: number) => setOrders(await listOrders(id));

  const open = async () => {
    if (!apptId) return;
    try {
      const e = await openEncounter(apptId);
      setEncounter(e);
      await refreshOrders(e.id);
      diagForm.setFieldsValue({
        chiefComplaint: e.chiefComplaint,
        diagnosisCode: e.diagnosisCode,
        diagnosisName: e.diagnosisName,
      });
      message.success(`接诊成功,就诊号 ${e.id}`);
    } catch {
      /* handled */
    }
  };

  const onDiagnose = async (v: any) => {
    if (!encounter) return;
    const e = await recordDiagnosis(encounter.id, v);
    setEncounter(e);
    message.success('诊断已记录');
  };

  const onAddOrder = async (v: any) => {
    if (!encounter) return;
    await addOrder(encounter.id, v);
    orderForm.resetFields();
    await refreshOrders(encounter.id);
    message.success('医嘱已添加');
  };

  const onSubmit = async () => {
    if (!encounter) return;
    await submitOrders(encounter.id);
    await refreshOrders(encounter.id);
    message.success('已提交,触发计费 / 审方');
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '类型', dataIndex: 'orderType', render: (v: string) => <Tag>{v}</Tag> },
    { title: '项目', dataIndex: 'itemName' },
    { title: '数量', dataIndex: 'quantity', width: 70 },
    { title: '单价', dataIndex: 'unitPrice', render: (v: number) => `¥${v}` },
    { title: '金额', dataIndex: 'amount', render: (v: number) => `¥${v}` },
    {
      title: '状态',
      dataIndex: 'state',
      render: (v: string) => <StatusTag status={v} />,
    },
    {
      title: '操作',
      render: (_: any, r: Order) => (
        <Space>
          {r.state === 'REJECTED' && (
            <Button type="link" size="small" onClick={async () => {
              await resubmitOrder(r.encounterId, r.id);
              await refreshOrders(r.encounterId);
              message.success('已重新送审');
            }}>
              重新送审
            </Button>
          )}
          {(r.state === 'CREATED' || r.state === 'REJECTED') && (
            <Popconfirm title="作废该医嘱?" onConfirm={async () => {
              await cancelOrder(r.encounterId, r.id);
              await refreshOrders(r.encounterId);
              message.success('已作废');
            }}>
              <Button type="link" size="small" danger>作废</Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <PageContainer title="门诊医生站" subtitle="接诊、记录诊断、开立与提交医嘱">
      <Card title="接诊" variant="borderless">
        <Space>
          挂号ID：
          <InputNumber value={apptId ?? undefined} onChange={(v) => setApptId(v)} onPressEnter={open} placeholder="挂号ID" />
          <Button type="primary" onClick={open}>接诊 / 打开就诊</Button>
        </Space>
      </Card>

      {encounter && (
        <>
          <Card title={`就诊 #${encounter.id}`} variant="borderless">
            <Descriptions size="small" column={3} bordered>
              <Descriptions.Item label="患者ID">{encounter.patientId}</Descriptions.Item>
              <Descriptions.Item label="科室">{encounter.deptCode}</Descriptions.Item>
              <Descriptions.Item label="医生">{encounter.doctorName}</Descriptions.Item>
              <Descriptions.Item label="状态" span={3}>
                <StatusTag status={encounter.status} />
              </Descriptions.Item>
            </Descriptions>

            <Divider orientation="left">诊断</Divider>
            <Form form={diagForm} layout="vertical" onFinish={onDiagnose} style={{ maxWidth: 640 }}>
              <Form.Item name="chiefComplaint" label="主诉">
                <Input.TextArea rows={2} placeholder="主诉" />
              </Form.Item>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="diagnosisCode" label="诊断编码(ICD)" rules={[{ required: true }]}>
                    <Input placeholder="如 J00" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="diagnosisName" label="诊断名称" rules={[{ required: true }]}>
                    <Input placeholder="如 急性鼻咽炎" />
                  </Form.Item>
                </Col>
              </Row>
              <Button type="primary" htmlType="submit">保存诊断</Button>
            </Form>
          </Card>

          <Card
            title="医嘱"
            variant="borderless"
            extra={
              <Popconfirm title="提交全部医嘱?药品将进入审方,其余直接计费。" onConfirm={onSubmit}>
                <Button type="primary">提交医嘱</Button>
              </Popconfirm>
            }
          >
            <Form form={orderForm} layout="inline" onFinish={onAddOrder} style={{ marginBottom: 16, rowGap: 12 }}>
              <Form.Item name="orderType" rules={[{ required: true }]} initialValue="DRUG">
                <Select options={ORDER_TYPES} style={{ width: 180 }} />
              </Form.Item>
              <Form.Item name="itemCode" rules={[{ required: true }]}>
                <Input placeholder="项目编码" />
              </Form.Item>
              <Form.Item name="itemName" rules={[{ required: true }]}>
                <Input placeholder="项目名称" />
              </Form.Item>
              <Form.Item name="quantity" rules={[{ required: true }]} initialValue={1}>
                <InputNumber placeholder="数量" min={1} />
              </Form.Item>
              <Form.Item name="unitPrice" rules={[{ required: true }]} initialValue={10}>
                <InputNumber placeholder="单价" min={0} prefix="¥" />
              </Form.Item>
              <Button htmlType="submit">添加医嘱</Button>
            </Form>
            <Table rowKey="id" columns={columns} dataSource={orders} size="middle" pagination={false} />
          </Card>
        </>
      )}
    </PageContainer>
  );
}
