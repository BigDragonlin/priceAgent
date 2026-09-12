# 前后端目录与运行方法

所有代码都在 priceAgent 内：

```text
priceAgent/
├── frontend/  前端页面、静态资源和前端依赖
├── backend/   Java 后端源码
│   └── .local/  本机启动配置、备份和日志
├── flow/      总体计划和进展
└── docs/      项目文档
```

## 启动后端

先打开 Docker，然后运行：

```sh
cd /Users/meng/Documents/priceAgent/backend
docker compose -f .local/compose.yaml up -d mysql redis backend
```

## 启动前端

```sh
cd /Users/meng/Documents/priceAgent/frontend
VITE_OPEN=false pnpm dev --host 127.0.0.1 --port 5173
```

页面地址为 `http://127.0.0.1:5173`，后端地址仍为 `http://localhost:48080`。

## 停止

前端在启动终端按 Ctrl+C。后端停止命令：

```sh
cd /Users/meng/Documents/priceAgent/backend
docker compose -f .local/compose.yaml stop
```

数据库继续使用原来的 Docker 数据卷，stop 不删除数据。

## Git 与源码

根目录 Git 管理前端与公共文档；backend 保留原来的独立 Git 历史，根 Git 忽略 backend，后端改动需在 backend 中查看与提交。此次仅整理文件夹，没有合并历史、暂存或提交。

后端的编译等操作见 [后端运行说明](backend/.local/README.md)。
