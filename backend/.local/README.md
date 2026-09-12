# 本机后台服务

后端源码位于本目录的上一级 backend；这里保存本机运行配置、日志和备份。按用户要求，运行配置（含本地密码和 API 密钥）纳入根 Git；日志和备份不提交。

- 后端源码：<https://github.com/YunaiV/ruoyi-vue-pro>，`master-jdk17` 分支，提交 `8e43004cf68a405cd3485f98f8a539b97ca6544a`，版本 `2026.08-SNAPSHOT`。
- 官方启动说明：<https://doc.iocoder.cn/quick-start/>。
- 启用官方默认的系统管理和基础设施模块。报价 Agent 业务尚未在这里实现。
- 数据库和缓存属于 `priceagent-local`，与其他项目分开；只把后台接口开放到本机。

后端命令都在 `/Users/meng/Documents/priceAgent/backend` 目录运行。

## 启动

先打开 Docker 应用，再启动数据库、缓存和后台：

```sh
docker compose -f .local/compose.yaml up -d mysql redis backend
```

网页在前端目录单独启动（在终端运行期间保持打开）：

```sh
cd /Users/meng/Documents/priceAgent/frontend
VITE_OPEN=false pnpm dev --host 127.0.0.1 --port 5173
```

网页地址：<http://127.0.0.1:5173>。后台接口：<http://localhost:48080>。

本地初始化账号：租户 `金茂源`，用户名 `admin`，密码 `admin123`。

## 查看运行情况和停止

```sh
docker compose -f .local/compose.yaml ps
docker compose -f .local/compose.yaml logs --tail 80 backend
docker compose -f .local/compose.yaml stop
```

`stop` 会停止本项目后台并保留数据库。网页在启动终端按 `Ctrl+C` 停止。

## 后端重新编译

源码发生改动后，先停止后台，再编译并重新启动：

```sh
docker compose -f .local/compose.yaml stop backend
docker compose -f .local/compose.yaml run --rm build
docker compose -f .local/compose.yaml up -d backend
```

Java、Maven、MySQL 和 Redis 均在 Docker 容器内运行，无需安装到系统中。
