import axios, { AxiosError, AxiosResponse } from 'axios';
import { message } from 'antd';
import type { Result } from './types';
import { IS_IDP } from '../auth/config';

// 统一 axios 实例。请求拦截器注入 Bearer Token;响应拦截器解包 Result<T>,
// 业务码非 0 统一弹错并抛出;401 清登录态跳登录页。
const client = axios.create({ timeout: 15000 });

export const TOKEN_KEY = 'his_token';

client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let redirecting = false;
function gotoLogin() {
  if (redirecting) return;
  redirecting = true;
  // idp 模式只清应用会话键,保留 oidc-client 自身登录态(IdP 会话或可静默续期);
  // password 模式沿用整体清理。
  if (IS_IDP) {
    localStorage.removeItem('his_token');
    localStorage.removeItem('his_user');
  } else {
    localStorage.clear();
  }
  window.location.href = '/login';
}

client.interceptors.response.use(
  (resp) => {
    // 业务接口统一解包 Result<T>,Promise 直接 resolve 到 data;
    // FHIR 端点返回裸字符串 JSON,原样透出。用 R 泛型在调用处声明真实返回类型。
    const ct = String(resp.headers['content-type'] ?? '');
    if (typeof resp.data === 'string' || ct.includes('fhir+json')) {
      return resp.data as unknown as AxiosResponse;
    }
    const body = resp.data as Result<unknown>;
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== 0) {
        message.error(body.message || '请求失败');
        return Promise.reject(new Error(body.message));
      }
      return body.data as unknown as AxiosResponse;
    }
    return resp.data as unknown as AxiosResponse;
  },
  (error: AxiosError<Result<unknown>>) => {
    const status = error.response?.status;
    const msg = error.response?.data?.message;
    if (status === 401) {
      message.error('登录已过期,请重新登录');
      gotoLogin();
    } else if (status === 429) {
      message.warning(msg || '请求过于频繁,已被限流');
    } else {
      message.error(msg || error.message || '网络错误');
    }
    return Promise.reject(error);
  },
);

export default client;
