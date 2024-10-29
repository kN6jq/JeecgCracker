# JeecgCracker

JeecgCracker 是一个专门针对 JeecgBoot 框架的密码爆破工具。

通过sql注入或者其他方法获取到账户的密码和盐,跑明文密码的工具

## 功能特点

- 🚀 多线程高性能设计
- 📊 实时进度和速度显示
- 🛡️ 智能错误处理机制
- 📝 详细的统计信息
- ⚡️ 支持自定义字典
- 🔧 可配置的线程数量

## 使用方法

### 命令格式
```bash
java -jar JeecgCracker.jar <字典文件路径> <salt> <目标密码哈希> <线程数>
```

### 参数说明
- `字典文件路径`: 密码字典文件的路径
- `salt`: 加密用的盐值
- `目标密码哈希`: 需要测试的密码哈希值
- `线程数`: 并发线程数量（建议设置为 CPU 核心数）

### 使用示例
```bash
java -jar JeecgCracker.jar dict.txt RCGTeGiH cb362cfeefbf3d8d 4
```

### 输出说明
```
进度: 45.5% (45500/100000) 速度: 15000 密码/秒 无效密码: 0

----------------------------------------
🎉 破解成功!
密码: yourpassword
哈希: cb362cfeefbf3d8d
尝试次数: 45500
----------------------------------------
```

## 配置建议

### 推荐配置
- JDK 版本: 1.8 或更高
- 内存: 建议 4GB 或更高
- CPU: 推荐多核处理器

### 线程数设置
- 对于 4 核 CPU: 建议使用 4 线程
- 对于 8 核 CPU: 建议使用 6-8 线程
- 服务器环境: 可根据实际情况调整，通常不超过 CPU 核心数

### 字典文件要求
- 编码: UTF-8
- 格式: 每行一个密码
- 建议大小: 根据实际需求决定

## 注意事项

1. 本工具仅用于安全测试和研究目的
2. 使用前请确保您有相关系统的测试授权
3. 建议在测试环境中使用
4. 字典文件请使用 ASCII 字符，非 ASCII 字符会被自动跳过
5. 大文件处理时建议适当增加 JVM 内存：
   ```bash
   java -Xmx4G -jar JeecgCracker.jar dict.txt RCGTeGiH cb362cfeefbf3d8d 4
   ```

## 高级用法

### 性能优化
```bash
# 使用更大的堆内存
java -Xmx8G -jar JeecgCracker.jar dict.txt RCGTeGiH cb362cfeefbf3d8d 8

# 设置最小堆内存
java -Xms4G -Xmx8G -jar JeecgCracker.jar dict.txt RCGTeGiH cb362cfeefbf3d8d 8
```

### 错误处理
- 工具会自动跳过无效密码
- 详细的错误统计
- 实时显示无效密码数量

## 构建说明

1. 克隆代码库
```bash
git clone https://github.com/kN6jq/JeecgCracker.git
```

2. 使用 Maven 构建
```bash
mvn clean package
```

3. 在 target 目录找到生成的 jar 文件

## License

[MIT License](LICENSE)