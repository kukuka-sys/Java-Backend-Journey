# 点评项目复习练习

这是一个独立的 Spring Boot 后端练习工程。`../hm-dianping` 是已有参考项目，本工程不复用其中的业务代码。练习时由你实现业务；我负责需求拆分、通用骨架、排错与代码 Review。

## 运行骨架

环境：JDK 21、Maven。

```powershell
cd E:\Java\dianping\dianping-practice
mvn spring-boot:run
```

访问 `http://localhost:8081/api/health`，预期得到 `{"success":true,"data":"UP","message":null}`。工程已加入 MyBatis-Plus、MySQL 和 Redis 依赖，启动前需要准备数据库并设置连接密码。连接密码通过环境变量提供，不提交到项目文件。

### 用户表与数据库连接

用户表建表脚本位于 `src/main/resources/db/001_create_user.sql`，使用独立数据库 `dianping_practice` 和表 `tb_user`。项目已配置 MyBatis-Plus 和 MySQL 驱动，下划线字段会映射到驼峰属性，例如 `user_id` 对应 `userId`。`UserMapper` 已声明为 `BaseMapper<User>` 接口。运行前在当前 PowerShell 会话设置数据库密码：

```powershell
$env:DB_PASSWORD = '你的本地 MySQL 密码'
mysql -u root -p --default-character-set=utf8mb4 -e "source E:/Java/dianping/dianping-practice/src/main/resources/db/001_create_user.sql"
mvn spring-boot:run
```

`mysql -p` 会单独提示输入密码。如数据库账号不是 `root`，还要设置 `DB_USERNAME`，并把建表命令中的 `-u root` 改为相应账号。当前用户表与 `User` 类一致，不包含密码字段。

### Redis 连接

`application.yaml` 中的 Redis 地址使用你提供的服务器地址，密码改由当前会话的 `REDIS_PASSWORD` 环境变量提供：

```powershell
$env:REDIS_PASSWORD = '你的 Redis 密码'
```

连接池配置需要 `commons-pool2`，已加入 `pom.xml`。请不要把真实密码写回 `application.yaml`。

## 练习边界

- 你写：数据表设计、接口、DTO、Service、Mapper/Repository、缓存与并发逻辑。
- 我协助：确认需求、检查设计、Review 代码、定位错误；需要时可以示范某一小段实现。
- 不要求一次做完。每轮完成后先验证，再进入下一轮。

## 第一轮：用户与店铺

### 用户接口约定

| 方法 | 路径 | 请求 |
| --- | --- | --- |
| POST | `/user/code?phone=13800138000` | 手机号作为查询参数，返回验证码发送结果 |
| POST | `/user/login` | JSON：`{"loginName":"13800138000","code":"123456"}`，成功时返回 token |

上述路径与登录拦截器的公开路径一致。后端直接访问使用这些路径；通过现有 Nginx 前端访问时，代理会在转发前去掉 `/api` 前缀。

### 用户

1. 手机号格式合法时可以请求验证码；验证码有有效期，过期或错误不能登录。
2. 首次登录创建用户，再次登录使用原账号。
3. 登录后能获取当前用户信息，也能退出。退出后的凭证不能继续使用。
4. 未登录用户可以浏览店铺，不能执行需要身份的操作。

练习时验证码可以在开发环境返回或写入本地日志，无须接入真实短信服务。

### 店铺

1. 获取店铺分类列表。
2. 按分类分页浏览店铺，返回名称、图片、地址、人均价格。
3. 根据 ID 查看店铺详情，包含介绍、营业时间等信息。
4. 分类或店铺不存在、页码不合法时返回清楚的错误响应。

**验收：**能演示完整登录和店铺浏览流程；接口参数、错误状态、响应结构一致；能解释数据模型与登录状态的设计。

## 第二轮：探店笔记

登录用户发布笔记（标题、内容、关联店铺、图片）；用户能分页浏览和查看详情；登录用户能点赞与取消点赞，同一用户的重复操作不会让点赞数出错。

**验收：**无效店铺不能发布；未登录不能发布或点赞；分页和点赞数正确。

## 第三轮：关注与动态

用户能关注、取消关注其他用户，不能关注自己；能查看关注列表以及关注者发布的新笔记。

**验收：**重复关注不产生重复关系；取消后动态列表不再包含该用户的新内容。

## 第四轮：优惠券与秒杀

用户能查看店铺优惠券；在有效时间内领取限量券；每人每券只能领取一次，库存不能为负；用户能查询自己的领取记录。

**验收：**验证未开始、已结束、售罄和重复领取；并发请求下最终领取数不超过库存。

## 每轮提交给我 Review 时

告诉我你完成了哪些接口、怎样运行和验证、目前卡在哪里。我会先指出问题和提示；你希望看示范代码时再直接写。
