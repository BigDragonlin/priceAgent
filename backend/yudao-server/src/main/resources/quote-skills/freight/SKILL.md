# 运输子 Skill

读取 GET /admin-api/quote/catalog 的 regions 和产品 weightKg。
整单运费 = 地区起步费 + 所有产品总重量 × 每公斤运价。
运费按数量分摊进入各产品售价，不再额外收一遍。当前为示例价，不是承运商实时价。
