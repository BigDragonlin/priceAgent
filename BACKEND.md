# 前后端目录说明

本目录 priceAgent 保存前端页面，配套后端源码放在同级目录：

```text
/Users/meng/Documents/
├── priceAgent/          前端
└── priceAgent-backend/  后端（独立 Git 仓库）
```

后端启动配置、备份和操作说明位于 `../priceAgent-backend/.local/`。

启动后端：

```sh
cd /Users/meng/Documents/priceAgent-backend
docker compose -f .local/compose.yaml up -d mysql redis backend
```

停止后端并保留数据：

```sh
cd /Users/meng/Documents/priceAgent-backend
docker compose -f .local/compose.yaml stop
```

本地接口地址仍为 `http://localhost:48080`，数据库继续使用原来的 Docker 数据卷。
