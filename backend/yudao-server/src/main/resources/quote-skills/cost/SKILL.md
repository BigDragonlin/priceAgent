# 成本子 Skill

读取 GET /admin-api/quote/catalog 的 products 和 rules。
成本 = （原料单价 ×（1 + 原料损耗率）+ 加工单价 + 包装单价）× 数量。
金额人民币，成本均不含税；AI 不允许修改这些基础值。缺资料时报告问题，不猜数字。
