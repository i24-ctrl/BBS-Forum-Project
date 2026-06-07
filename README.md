# 🏫 校内BBS论坛系统

> **Web程序设计(Java EE) 课程大作业**
>
> 基于 Spring Boot 2.7.18 + MyBatis-Plus 3.5.7 + Thymeleaf + MySQL 8.0 + Redis 6.x 构建的校内BBS社区平台。

---

## 📋 功能模块

| 模块 | 功能 | 负责人 |
|------|------|--------|
| 用户模块 | 注册/登录/JWT认证/个人资料/积分流水 | B |
| 板块帖子 | 板块浏览/发帖/富文本编辑/回复/楼中楼 | C |
| 高级管理 | 置顶/加精/管理员日志AOP/用户审核 | D |
| 需求积分 | 悬赏发布/采纳/积分冻结/转账/乐观锁 | E |
| 公共基础 | 项目骨架/JWT/Redis/全局异常/拦截器 | A（组长） |

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| MyBatis-Plus | 3.5.7 | ORM + 分页 + 逻辑删除 |
| Thymeleaf | 2.7.18 (内置) | 模板引擎 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 6.x+ | 缓存（预留） |
| Spring Security | 2.7.18 | BCrypt 密码编码 |
| jjwt | 0.11.5 | JWT 令牌 |
| wangEditor | 5.1.23 | 富文本编辑器 |
| Jsoup | 1.17.2 | XSS 过滤 |
| Druid | 1.2.20 | 数据库连接池 |
| Bootstrap | 5.3 | 前端 UI |

---

## 🚀 快速启动

### 环境要求

- **JDK 11** 或更高
- **MySQL 8.0** 或更高
- **Redis 6.x** 或更高（可选，暂未强依赖）
- **Maven 3.6+**

### 1. 创建数据库

```sql
-- 在 MySQL 中执行
CREATE DATABASE IF NOT EXISTS `bbs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;