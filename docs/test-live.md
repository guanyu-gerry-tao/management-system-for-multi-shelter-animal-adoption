# 实机测试计划（Claude Agent 执行）

测试目的：在 demo 前验证系统全流程无误。由 Claude Code 作为 AI agent，解读自然语言并执行 `shelter` CLI 命令。

**前置条件**（在项目根目录 `management-system-for-multi-shelter-animal-adoption/` 执行）：
```bash
./gradlew installDist                                  # 构建二进制
export PATH="$PWD/build/install/shelter/bin:$PATH"    # 注册 shelter 命令
rm -rf ~/shelter/data                                  # 清空历史数据，保证干净状态
shelter --version                                      # 验证 CLI 可用
```

---

## 阶段一：庇护所管理（UC-01）

**对 Claude 说：**
> 注册两个庇护所：第一个叫 "Boston Paws"，位于 Boston，容量 15；第二个叫 "Cambridge Care"，位于 Cambridge，容量 10。

✅ 期望：两行 `Registered shelter: ... (id=...)` 输出

**对 Claude 说：**
> 列出所有庇护所。

✅ 期望：显示两行记录，有 ID、Name、Location、Capacity 列

**对 Claude 说：**
> 把 Boston Paws 的容量改成 20。

✅ 期望：`Updated shelter: Boston Paws (id=...)`

**对 Claude 说：**
> 试着删除 Boston Paws。

✅ 期望：报错（因为后续会往里面放动物，此处可先跳过或验证空庇护所可删后重建）

---

## 阶段二：动物管理（UC-02）

**对 Claude 说：**
> 往 Boston Paws 收容一只 3 岁的拉布拉多犬，叫 Max，活跃度 HIGH。

✅ 期望：`Admitted dog: Max (id=..., shelter=...)`

**对 Claude 说：**
> 再往 Boston Paws 收容一只 2 岁的暹罗猫，叫 Luna，活跃度 LOW，室内猫。

✅ 期望：`Admitted cat: Luna (id=..., shelter=...)`

**对 Claude 说：**
> 往 Cambridge Care 收容一只 1 岁的荷兰垂耳兔，叫 Fluffy，活跃度 MEDIUM。

✅ 期望：`Admitted rabbit: Fluffy (id=..., shelter=...)`

**对 Claude 说：**
> 往 Boston Paws 再收容一条热带鱼，叫 Nemo，品种 Clownfish，活跃度 LOW。

✅ 期望：`Admitted fish: Nemo (id=..., shelter=...)`（species 显示 fish）

**对 Claude 说：**
> 列出 Boston Paws 里的所有动物。

✅ 期望：Max、Luna、Nemo 出现，Fluffy 不出现

**对 Claude 说：**
> 把 Max 的名字改成 Buddy。

✅ 期望：`Updated animal: Buddy (id=...)`

**对 Claude 说：**
> 列出所有动物（不过滤庇护所）。

✅ 期望：Buddy、Luna、Fluffy、Nemo 全部出现

---

## 阶段三：领养人管理（UC-03）

**对 Claude 说：**
> 注册领养人 Alice，住 HOUSE_WITH_YARD，日程 HOME_MOST_OF_DAY，偏好 DOG，年龄范围 1 到 5 岁。

✅ 期望：`Registered adopter: Alice (id=...)`

**对 Claude 说：**
> 注册领养人 Bob，住 APARTMENT，日程 AWAY_PART_OF_DAY，无特定偏好。

✅ 期望：`Registered adopter: Bob (id=...)`

**对 Claude 说：**
> 列出所有领养人。

✅ 期望：Alice 和 Bob 都出现

**对 Claude 说：**
> 把 Bob 的日程改成 HOME_MOST_OF_DAY。

✅ 期望：`Updated adopter: Bob (id=...)`

---

## 阶段四：匹配（UC-04）

**对 Claude 说：**
> 为 Alice 在 Boston Paws 里做动物匹配。

✅ 期望：显示 Rank/Animal ID/Name/Score 表格，Buddy 应该得分最高（狗+年龄符合）

**对 Claude 说：**
> 为 Buddy 匹配合适的领养人。

✅ 期望：显示 Rank/Adopter ID/Name/Score 表格，Alice 应排名靠前

---

## 阶段五：领养流程（UC-05）

**对 Claude 说：**
> Alice 想领养 Buddy，提交申请。

✅ 期望：`Submitted adoption request: id=... (adopter=..., animal=...)`

**对 Claude 说：**
> 批准这个领养申请。

✅ 期望：`Approved adoption request: ...`

**对 Claude 说：**
> 列出 Boston Paws 的动物，看看 Buddy 状态。

✅ 期望：Buddy 一行显示 `adopted`，其他动物显示 `available`

**对 Claude 说：**
> 再试一次为 Buddy 提交 Bob 的领养申请。

✅ 期望：报错（动物已被领养）

**对 Claude 说：**
> Bob 想领养 Luna，提交申请，然后拒绝它。

✅ 期望：submit 成功，reject 输出 `Rejected adoption request: ...`

**对 Claude 说：**
> Bob 再提交一个领养 Luna 的申请，然后取消。

✅ 期望：submit 成功，cancel 输出 `Cancelled adoption request: ...`

---

## 阶段六：转移流程（UC-06）

**对 Claude 说：**
> 把 Luna 从 Boston Paws 转移到 Cambridge Care。

✅ 期望：`Transfer request created: id=... (animal=..., Boston Paws → Cambridge Care)`

**对 Claude 说：**
> 批准这个转移请求。

✅ 期望：`Approved transfer request: ...`

**对 Claude 说：**
> 列出 Cambridge Care 的动物。

✅ 期望：Luna 和 Fluffy 都出现

**对 Claude 说：**
> 把 Nemo 从 Boston Paws 转移到 Cambridge Care，然后拒绝。

✅ 期望：request 成功，reject 输出 `Rejected transfer request: ...`

---

## 阶段七：疫苗管理（UC-07）

**对 Claude 说：**
> 添加两种疫苗类型：Rabies，适用于 DOG，有效期 365 天；Feline FVRCP，适用于 CAT，有效期 365 天。

✅ 期望：两行 `Added vaccine type: ...`

**对 Claude 说：**
> 列出所有疫苗类型。

✅ 期望：显示 Rabies 和 Feline FVRCP

**对 Claude 说：**
> 给 Buddy 接种 Rabies 疫苗，日期今天。

✅ 期望：`Recorded vaccination: animal=..., type=Rabies, date=...`

**对 Claude 说：**
> 检查 Buddy 有没有过期疫苗。

✅ 期望：`All vaccinations are current for animal: ...`

**对 Claude 说：**
> 给 Luna 接种 Rabies（狗专用疫苗）。

✅ 期望：报错（物种不匹配）

**对 Claude 说：**
> 把 Feline FVRCP 改名为 Cat FVRCP。

✅ 期望：`Updated vaccine type: Cat FVRCP (id=...)`

---

## 阶段八：审计日志（UC-08）

**对 Claude 说：**
> 查看审计日志。

✅ 期望：显示 Timestamp/Staff/Action/Target 表格，包含本次测试所有操作记录

---

## 阶段九：边界错误验证

**对 Claude 说：**
> 删除 Fluffy（Fluffy 没有 pending 申请，应该可以删）。

✅ 期望：`Removed animal: ...`

**对 Claude 说：**
> 删除 Cambridge Care（里面还有 Luna，不能删）。

✅ 期望：报错（shelter still holds animals）

**对 Claude 说：**
> 查询不存在的庇护所 ID "abc-123"。

✅ 期望：报错（not found）

---

## 测试通过标准

- [ ] 所有 ✅ 项输出符合预期
- [ ] 所有错误场景正确报错，不崩溃
- [ ] `shelter audit log` 显示完整操作历史
- [ ] 重启后（重新执行任意 list 命令）数据仍然存在
