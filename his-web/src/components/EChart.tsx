import ReactECharts from 'echarts-for-react';
import type { EChartsOption } from 'echarts';
import { theme } from 'antd';

// ECharts 封装:背景透明、文字跟随主题 token、自适应宽高。
// 坐标轴/分割线颜色由各图在 option 内用 token 指定,保证亮暗都清晰。
interface Props {
  option: EChartsOption;
  height?: number;
}

export default function EChart({ option, height = 300 }: Props) {
  const { token } = theme.useToken();

  const themed: EChartsOption = {
    backgroundColor: 'transparent',
    textStyle: { color: token.colorTextSecondary },
    ...option,
  };

  return (
    <ReactECharts
      option={themed}
      style={{ height }}
      notMerge
      lazyUpdate
      opts={{ renderer: 'canvas' }}
    />
  );
}
