import client from './client';
import type {
  Appointment,
  Dictionary,
  Encounter,
  Invoice,
  LoginResponse,
  Notification,
  Order,
  Patient,
  PendingReview,
  RefundRequest,
  Schedule,
} from './types';

// 路径与各服务 @RequestMapping 一致,经网关前缀路由。

// ---- auth ----
export const login = (username: string, password: string) =>
  client.post<unknown, LoginResponse>('/auth/login', { username, password });

// ---- mdm 主数据 ----
export const createPatient = (body: {
  name: string;
  gender: 'MALE' | 'FEMALE' | 'UNKNOWN';
  birthDate?: string;
  idCard: string;
  phone?: string;
  address?: string;
}) => client.post<unknown, Patient>('/mdm/patients', body);

export const getPatient = (id: number) => client.get<unknown, Patient>(`/mdm/patients/${id}`);

export const listDict = (type: string) => client.get<unknown, Dictionary[]>(`/mdm/dicts/${type}`);

// ---- registration 挂号 ----
export const openSchedule = (body: {
  doctorId: number;
  doctorName: string;
  deptCode: string;
  scheduleDate: string;
  period: 'AM' | 'PM';
  totalSlots: number;
  fee: number;
}) => client.post<unknown, Schedule>('/reg/schedules', body);

export const listSchedules = (date: string, deptCode: string) =>
  client.get<unknown, Schedule[]>('/reg/schedules', { params: { date, deptCode } });

export const book = (scheduleId: number, patientId: number) =>
  client.post<unknown, Appointment>('/reg/appointments', { scheduleId, patientId });

export const getAppointment = (id: number) =>
  client.get<unknown, Appointment>(`/reg/appointments/${id}`);

// ---- outpatient 门诊医生站 ----
export const openEncounter = (appointmentId: number) =>
  client.post<unknown, Encounter>('/outpatient/encounters', { appointmentId });

export const getEncounter = (id: number) =>
  client.get<unknown, Encounter>(`/outpatient/encounters/${id}`);

export const recordDiagnosis = (
  id: number,
  body: { chiefComplaint?: string; diagnosisCode: string; diagnosisName: string },
) => client.put<unknown, Encounter>(`/outpatient/encounters/${id}/diagnosis`, body);

export const addOrder = (
  id: number,
  body: { orderType: string; itemCode: string; itemName: string; quantity: number; unitPrice: number },
) => client.post<unknown, Order>(`/outpatient/encounters/${id}/orders`, body);

export const listOrders = (id: number) =>
  client.get<unknown, Order[]>(`/outpatient/encounters/${id}/orders`);

export const submitOrders = (id: number) =>
  client.post<unknown, void>(`/outpatient/encounters/${id}/submit`);

export const resubmitOrder = (id: number, orderId: number) =>
  client.post<unknown, void>(`/outpatient/encounters/${id}/orders/${orderId}/resubmit`);

export const cancelOrder = (id: number, orderId: number) =>
  client.post<unknown, void>(`/outpatient/encounters/${id}/orders/${orderId}/cancel`);

// ---- 药师审方 ----
export const listPendingReviews = () =>
  client.get<unknown, PendingReview[]>('/outpatient/reviews/pending');

export const passReview = (encounterId: number) =>
  client.post<unknown, void>(`/outpatient/reviews/${encounterId}/pass`);

export const rejectReview = (encounterId: number, opinion: string) =>
  client.post<unknown, void>(`/outpatient/reviews/${encounterId}/reject`, { opinion });

// ---- billing 收费 ----
export const getInvoiceByEncounter = (encounterId: number) =>
  client.get<unknown, Invoice>(`/billing/invoices/by-encounter/${encounterId}`);

export const getInvoice = (id: number) => client.get<unknown, Invoice>(`/billing/invoices/${id}`);

export const payInvoice = (id: number, payMethod: string) =>
  client.post<unknown, Invoice>(`/billing/invoices/${id}/pay`, { payMethod });

// ---- 退费审批 ----
export const requestRefund = (invoiceId: number, reason: string) =>
  client.post<unknown, RefundRequest>(`/billing/invoices/${invoiceId}/refund-requests`, { reason });

export const listPendingRefunds = () =>
  client.get<unknown, RefundRequest[]>('/billing/refund-requests/pending');

export const approveRefund = (id: number) =>
  client.post<unknown, RefundRequest>(`/billing/refund-requests/${id}/approve`);

export const rejectRefund = (id: number, opinion: string) =>
  client.post<unknown, RefundRequest>(`/billing/refund-requests/${id}/reject`, { opinion });

// ---- emr 病历 / FHIR ----
export const generateEmr = (encounterId: number) =>
  client.post<unknown, number>(`/emr/documents/${encounterId}`);

export const getEmrDocument = (encounterId: number) =>
  client.get<unknown, string>(`/emr/documents/${encounterId}`, { responseType: 'text' });

// ---- notify 通知中心 ----
export const inbox = (onlyUnread = false) =>
  client.get<unknown, Notification[]>('/notify/inbox', { params: { onlyUnread } });

export const unreadCount = () =>
  client.get<unknown, { count: number }>('/notify/unread-count');

export const markRead = (id: number) => client.post<unknown, void>(`/notify/${id}/read`);

export const markAllRead = () =>
  client.post<unknown, { updated: number }>('/notify/read-all');
