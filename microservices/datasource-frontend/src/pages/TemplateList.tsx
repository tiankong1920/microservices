import React from 'react'
import { Card, Typography } from 'antd'

const { Title, Paragraph } = Typography

const TemplateList: React.FC = () => {
  return (
    <div>
      <div className="page-header">
        <div className="page-title">配置模板</div>
        <div className="page-subtitle">管理数据源配置模板</div>
      </div>

      <Card>
        <Title level={4}>配置模板管理</Title>
        <Paragraph>
          此页面用于管理数据源配置模板，支持模板的创建、编辑、复制和共享功能。
        </Paragraph>
      </Card>
    </div>
  )
}

export default TemplateList
