# T01 报价 Demo

- 输入：本轮确认的模块化计算架构、总 Skill / 子 Skill 需求。
- 负责：Codex 实现，另一模型独立评审。
- 前端产出：`frontend/src/views/quote/`、`frontend/src/api/quote/`、报价页面路由。
- 后端产出：`backend/yudao-server/src/main/java/cn/iocoder/yudao/server/quote/`、报价控制器、`backend/yudao-server/src/main/resources/quote-skills/`。
- 验收：正常报价、毛利口径、硬性底线、预算冲突、确认建议后重算、编辑后旧结果失效、Excel 数字与页面一致。
- 约束：后端基础成本不可被浏览器/AI 任意覆盖，货币精确到分；未配置模型直接报错，不使用 Mock；对外文件不含成本利润。
