# Demo 计划

**日期**：2026-04-21 1:00–4:00 PM（课堂 demo + Q&A）
**操作员**：Claude Code（AI agent）解读自然语言 → 执行 `shelter` CLI
**建议时长**：正式 demo 约 15–20 分钟，预留 Q&A 时间

---

## 叙事背景

> 波士顿地区爆发了一场宠物救助热潮。
> **Boston Paws**（容量 15）和 **Cambridge Care**（容量 10）两家庇护所需要协作管理：
> 动物入住、领养人匹配、跨庇护所转移、疫苗合规——所有操作都通过一套统一的 CLI 工具完成。
> 今天，工作人员打开终端，Claude Code 作为 AI 助手，解读口令、执行命令。

---

## 准备工作（演示前完成）

```bash
./gradlew installDist          # 构建二进制
rm -rf ~/shelter/data          # 清空数据，干净环境
export PATH="$PWD/build/install/shelter/bin:$PATH"
shelter --version              # 验证 CLI 可用
```

---

## 场景一：庇护所上线（UC-01）⏱ 约 2 分钟

**口令** → **动作** → **亮点**

| # | 对 Claude 说 | Claude 执行的命令 | 演示亮点 |
|---|---|---|---|
| 1 | 注册两个庇护所：Boston Paws 在 Boston 容量 15，Cambridge Care 在 Cambridge 容量 10 | `shelter shelter register ...` × 2 | 数据持久化写入 CSV |
| 2 | 列出所有庇护所 | `shelter shelter list` | 表格显示 ID/名称/地点/容量 |
| 3 | 把 Boston Paws 的容量扩大到 20 | `shelter shelter update --id <id> --capacity 20` | 部分字段更新，未提供的字段保持不变 |

> **OOD 亮点**：
> - `ShelterApplicationService` 与 `ShelterService` 分层；
> - `AuditService.log("registered shelter", shelter)` 每次操作自动记录。

---

## 场景二：收容动物（UC-02）⏱ 约 3 分钟

| # | 对 Claude 说 | 演示亮点 |
|---|---|---|
| 1 | 往 Boston Paws 收容一只 3 岁拉布拉多犬 Buddy，活跃度 HIGH | `Dog` 多态子类，size/neutered 可选参数 |
| 2 | 再收容一只 2 岁暹罗猫 Luna，活跃度 LOW，室内猫 | `Cat` 子类，indoor 属性 |
| 3 | 往 Cambridge Care 收容一只 1 岁荷兰垂耳兔 Fluffy，活跃度 MEDIUM | 跨庇护所收容 |
| 4 | 往 Boston Paws 收容一条热带鱼 Nemo，品种 Clownfish，物种名 fish | `Other` 自由物种名，体现开放扩展 |
| 5 | 列出 Boston Paws 的动物 | 只显示该庇护所的动物，Fluffy 不出现 |

> **OOD 亮点**：
> - `Animal`（抽象）→ `Dog` / `Cat` / `Rabbit` / `Other`：继承 + 多态；
> - `Other` 无需修改枚举即可支持新物种，体现**开闭原则**；
> - 策略层分离：动物对象不包含任何评分逻辑。

---

## 场景三：登记领养人（UC-03）⏱ 约 2 分钟

| # | 对 Claude 说 | 演示亮点 |
|---|---|---|
| 1 | 注册 Alice：HOUSE_WITH_YARD，HOME_MOST_OF_DAY，偏好 DOG / Labrador / HIGH，要求已接种，年龄 1–5 岁 | `AdopterPreferences` 封装全部偏好字段 |
| 2 | 注册 Bob：APARTMENT，AWAY_PART_OF_DAY，偏好 CAT / LOW，不要求接种，年龄不限 | 对比展示稀疏偏好 |
| 3 | 把 Bob 的日程改为 HOME_MOST_OF_DAY，活跃度偏好改为 MEDIUM | 部分字段更新，其余保持不变 |

---

## 场景四：智能匹配（UC-04）⏱ 约 3 分钟  ★ 核心亮点

| # | 对 Claude 说 | 演示亮点 |
|---|---|---|
| 1 | 为 Alice 在 Boston Paws 里做动物匹配 | 显示 Rank/Animal/Score 表格 |
| 2 | 解读结果：为什么 Buddy 排名第一？ | 讲解各 Strategy 的得分贡献 |
| 3 | 为 Buddy 匹配合适的领养人 | 反向匹配，Alice 应排名靠前 |

**展示各 Strategy 的贡献**（讲解时提及，无需执行命令）：

| 策略 | 作用 |
|---|---|
| `SpeciesPreferenceStrategy` | Alice 偏好 DOG → Buddy 得满分 |
| `AgePreferenceStrategy` | Alice 要求 1–5 岁，Buddy 3 岁 → 满分 |
| `ActivityLevelStrategy` | Buddy HIGH，Alice HOME_MOST_OF_DAY → 匹配 |
| `LifestyleCompatibilityStrategy` | HOUSE_WITH_YARD 适合大型犬 |
| `VaccinationPreferenceStrategy` | 若领养人要求已接种，未接种动物降分 |

> **OOD 亮点**：
> - **策略模式**：`IMatchingStrategy` 接口 → 6 个独立实现，可随时增减；
> - **开闭原则**：新增评分维度只需实现接口，无需改动 Service；
> - **依赖倒置**：`MatchingApplicationService` 依赖接口列表，不依赖具体策略。

---

## 场景五：领养流程（UC-05）⏱ 约 2 分钟

| # | 对 Claude 说 | 演示亮点 |
|---|---|---|
| 1 | Alice 想领养 Buddy，提交申请 | `AdoptionRequest` 状态机 PENDING |
| 2 | 批准这个申请 | 状态变 APPROVED，Buddy 标记为 adopted |
| 3 | 列出 Boston Paws 动物，看 Buddy 状态 | 表格显示 `adopted` vs `available` |
| 4 | 再试一次为 Bob 提交 Buddy 的领养申请 | 报错演示：动物已被领养 |

> **OOD 亮点**：
> - 状态机：PENDING → APPROVED / REJECTED / CANCELLED，状态只能单向流转；
> - `AdoptionService` 与 `RequestNotificationService` 解耦，通知逻辑可独立替换。

---

## 场景六：跨庇护所转移（UC-06）⏱ 约 2 分钟

| # | 对 Claude 说 | 演示亮点 |
|---|---|---|
| 1 | 把 Luna 从 Boston Paws 转移到 Cambridge Care | `TransferRequest` 创建，PENDING 状态 |
| 2 | 批准转移 | Luna 的 shelterId 变更，两庇护所占用更新 |
| 3 | 列出 Cambridge Care 的动物 | Luna 和 Fluffy 都出现 |

> **OOD 亮点**：与领养流程同构的状态机设计，体现 **DRY**（请求生命周期复用）。

---

## 场景七：疫苗合规（UC-07）⏱ 约 2 分钟

| # | 对 Claude 说 | 演示亮点 |
|---|---|---|
| 1 | 添加疫苗类型：Rabies（适用 DOG，365 天）；Feline FVRCP（适用 CAT，365 天） | `VaccineType` 独立目录，与动物解耦 |
| 2 | 给 Buddy 接种 Rabies，日期今天 | `VaccinationRecord` 记录 |
| 3 | 检查 Buddy 有没有过期疫苗 | 显示"All vaccinations are current" |
| 4 | 尝试给 Luna 接种 Rabies（狗专用） | 报错：物种不匹配，物种校验在 Service 层 |

> **OOD 亮点**：
> - `VaccinationPreferenceStrategy` 让疫苗状态影响匹配分数，体现层间协作；
> - 物种校验在 Service 层，体现**单一职责**。

---

## 场景八：完整审计（UC-08）⏱ 约 1 分钟

| # | 对 Claude 说 | 演示亮点 |
|---|---|---|
| 1 | 查看审计日志 | 显示 Timestamp/Staff/Action/Target 全量记录 |

> **OOD 亮点**：
> - `AuditService<T>` 泛型接口，每个应用服务注入专属实例；
> - 所有修改操作（admit/update/approve/...）均自动记录，无需调用方手动触发。

---

## 补充：错误处理演示（可选，30 秒）

如果时间允许，快速演示 1–2 个错误场景：

- 删除 Cambridge Care（还有 Luna）→ 报错"shelter still holds animals"
- 查询不存在的 ID → 报错"not found"

> **OOD 亮点**：早期报错原则；异常只用于真正异常情况，不用于流程控制。

---

## OOD 设计原则速查（Q&A 准备）

| 原则 | 在哪里体现 |
|---|---|
| 继承 + 多态 | `Animal` → `Dog/Cat/Rabbit/Other` |
| 开闭原则 | 策略模式（新增 Strategy 不改 Service）；`Other` 不改枚举即扩展物种 |
| 单一职责 | Application / Service / Strategy / Domain 各层独立 |
| 依赖倒置 | 接口注入：`IMatchingStrategy`, `AuditService<T>`, `ExplanationService` |
| DRY | `AdoptionRequest` 和 `TransferRequest` 共享状态机生命周期设计 |
| 接口稳定性 | 公开接口不变；实现可替换（`MockExplanationService` vs `AIExplanationService`） |
| 防御性拷贝 | `AdopterPreferences` 值对象，Copy constructor |
| 审计横切 | `AuditService<T>` 泛型接口，统一注入所有 Application Service |
| 状态机 | 请求状态 PENDING→APPROVED/REJECTED/CANCELLED 单向流转 |
| 策略评分隔离 | 评分逻辑不在 Domain，不在 Application，独立在 Strategy 层 |

---

## 幻灯片提纲（参考）

1. **标题页**：系统名、团队成员、CS 5004
2. **问题背景**：多庇护所协作痛点
3. **架构图**：五层架构（使用 `docs/diagram-layer.mmd`）
4. **类图亮点**：Animal 继承体系（使用 `docs/diagram-class.mmd`）
5. **LIVE DEMO**（按本文档顺序）
6. **测试覆盖**：78 个集成测试 + 单元测试，展示 `./gradlew test` 全绿
7. **OOD 原则总结**（上表）
8. **Q&A**

---

## 时间分配总览

| 环节 | 建议时长 |
|---|---|
| 开场 + 架构介绍（幻灯片） | 3 分钟 |
| 场景一：庇护所上线 | 2 分钟 |
| 场景二：收容动物 | 3 分钟 |
| 场景三：登记领养人 | 2 分钟 |
| 场景四：智能匹配 ★ | 3 分钟 |
| 场景五：领养流程 | 2 分钟 |
| 场景六：跨庇护所转移 | 2 分钟 |
| 场景七：疫苗合规 | 2 分钟 |
| 场景八：审计日志 + 错误处理 | 1 分钟 |
| 测试展示（`./gradlew test`） | 1 分钟 |
| **总计** | **≈ 21 分钟** |

---

## 注意事项

- **演示前**执行完整的 `docs/test-live.md` 流程，确保所有命令输出符合预期
- **备用策略**：若 AI agent 解析出错，可直接切换手动输入 CLI 命令（CLAUDE.md 有完整参考）
- **ID 获取**：每个阶段先执行 `list` 命令获取 ID，再执行目标命令
- **网络**：`MockExplanationService` 已默认使用，无需外部 API 调用，演示无需联网
