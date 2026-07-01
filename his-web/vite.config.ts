import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 前后端分离:开发期所有业务前缀经 dev proxy 直连网关 9000,避免 CORS。
// 生产部署改由 Nginx 同样反代这些前缀到网关即可,前端代码零改动。
// 用正则键 `^/prefix/` 要求前缀后必须有子路径,避免与 SPA 客户端路由
// (裸 /billing /outpatient /emr)冲突——真实 API 一律带子路径。
const gatewayPrefixes = ['auth', 'mdm', 'reg', 'outpatient', 'billing', 'emr', 'fhir', 'notify'];

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: Object.fromEntries(
      gatewayPrefixes.map((p) => [`^/${p}/`, { target: 'http://localhost:9000', changeOrigin: true }]),
    ),
  },
  build: {
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        // 按体积大的第三方库拆分,避免单一巨包,首屏与缓存都更友好。
        manualChunks: {
          react: ['react', 'react-dom', 'react-router-dom'],
          antd: ['antd', '@ant-design/icons'],
          echarts: ['echarts', 'echarts-for-react'],
          'pro-components': ['@ant-design/pro-components'],
        },
      },
    },
  },
});
