// 与后端 his-api DTO / 各服务 VO 对齐的前端类型。

export interface Result<T> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
  userId: number;
  username: string;
  realName: string;
  deptId: number | null;
  roles: string[];
}

export interface Patient {
  id: number;
  empiNo: string;
  name: string;
  gender: string;
  birthDate: string | null;
  idCard: string;
  phone: string;
}

export interface Dictionary {
  id: number;
  type: string;
  code: string;
  name: string;
  sort: number;
}

export interface Schedule {
  id: number;
  doctorId: number;
  doctorName: string;
  deptCode: string;
  scheduleDate: string;
  period: 'AM' | 'PM';
  totalSlots: number;
  bookedSlots: number;
  fee: number;
}

export interface Appointment {
  id: number;
  scheduleId: number;
  patientId: number;
  deptCode: string;
  doctorName: string;
  serialNo: number;
  fee: number;
  status: string;
}

export interface Encounter {
  id: number;
  appointmentId: number;
  patientId: number;
  deptCode: string;
  doctorId: number;
  doctorName: string;
  chiefComplaint: string | null;
  diagnosisCode: string | null;
  diagnosisName: string | null;
  status: string;
}

export interface Order {
  id: number;
  encounterId: number;
  orderType: string;
  itemCode: string;
  itemName: string;
  quantity: number;
  unitPrice: number;
  amount: number;
  state: string;
}

export interface InvoiceLine {
  orderId: number;
  itemCode: string;
  itemName: string;
  quantity: number;
  amount: number;
}

export interface Invoice {
  id: number;
  encounterId: number;
  patientId: number;
  deptCode: string;
  totalAmount: number;
  status: string;
  payMethod: string | null;
  paidAt: string | null;
  items: InvoiceLine[];
}

export interface RefundRequest {
  id: number;
  invoiceId: number;
  encounterId: number;
  amount: number;
  reason: string;
  status: string;
  opinion: string | null;
  applicantUserId: number;
  applicantName: string;
  reviewerName: string | null;
  reviewedAt: string | null;
}

export interface PendingReviewItem {
  orderId: number;
  itemCode: string;
  itemName: string;
  quantity: number;
  amount: number;
}

export interface PendingReview {
  encounterId: number;
  patientId: number;
  deptCode: string;
  doctorName: string;
  items: PendingReviewItem[];
}

export interface Notification {
  id: number;
  type: string;
  title: string;
  content: string;
  bizType: string;
  bizId: number;
  read: boolean;
  createdAt: string;
}
