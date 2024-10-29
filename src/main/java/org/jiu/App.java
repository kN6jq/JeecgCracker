package org.jiu;

import org.jiu.utils.PasswordUtil;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    // 检查字符串是否只包含ASCII字符
    private static boolean isAscii(String s) {
        return s.matches("^[\\x00-\\x7F]*$");
    }

    // 清理密码，移除空白字符并验证
    private static String cleanPassword(String password) {
        if (password == null) return null;
        String cleaned = password.trim();
        return cleaned.isEmpty() ? null : (isAscii(cleaned) ? cleaned : null);
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("使用方法: java -jar passwordcracker.jar <字典文件路径> <salt> <目标密码哈希> <线程数>");
            System.out.println("示例: java -jar passwordcracker.jar dict.txt RCGTeGiH cb362cfeefbf3d8d 4");
            System.exit(1);
        }

        String dictionaryPath = args[0];    // 字典文件路径
        String salt = args[1];              // 盐值
        String targetHash = args[2];        // 目标哈希值
        int threadCount;                    // 线程数

        try {
            threadCount = Integer.parseInt(args[3]);
            if (threadCount <= 0) {
                threadCount = Runtime.getRuntime().availableProcessors();
                System.out.println("线程数无效，使用CPU核心数: " + threadCount);
            }
        } catch (NumberFormatException e) {
            threadCount = Runtime.getRuntime().availableProcessors();
            System.out.println("线程数格式错误，使用CPU核心数: " + threadCount);
        }

        final String username = "admin";

        if (!Files.exists(Paths.get(dictionaryPath))) {
            System.out.println("错误: 找不到字典文件 " + dictionaryPath);
            System.exit(1);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicLong currentLine = new AtomicLong(0);
        AtomicLong invalidPasswords = new AtomicLong(0);

        try {
            System.out.println("正在读取并预处理字典文件...");

            // 读取并预处理密码列表，过滤掉非ASCII字符和空行
            List<String> allPasswords = Files.lines(Paths.get(dictionaryPath))
                    .map(App::cleanPassword)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            long totalLines = allPasswords.size();
            System.out.println("字典总密码数: " + totalLines);

            long startTime = System.currentTimeMillis();
            int chunkSize = (int) Math.ceil((double) allPasswords.size() / threadCount);
            List<Future<?>> futures = new ArrayList<>();

            System.out.println("使用 " + threadCount + " 个线程开始破解...");
            System.out.println("目标哈希: " + targetHash);
            System.out.println("使用盐值: " + salt);

            for (int i = 0; i < threadCount; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, allPasswords.size());
                List<String> passwordChunk = allPasswords.subList(start, end);

                futures.add(executor.submit(() -> {
                    for (String password : passwordChunk) {
                        if (found.get()) break;

                        try {
                            String hash = PasswordUtil.encrypt(username, password, salt);

                            long current = currentLine.incrementAndGet();
                            if (current % 1000 == 0 || current == totalLines) {
                                long elapsedTime = System.currentTimeMillis() - startTime;
                                double progress = (double) current / totalLines * 100;
                                long speed = current * 1000 / (elapsedTime + 1);
                                System.out.printf("\r进度: %.2f%% (%d/%d) 速度: %d 密码/秒 无效密码: %d",
                                        progress, current, totalLines, speed, invalidPasswords.get());
                            }

                            if (hash.equals(targetHash)) {
                                found.set(true);
                                System.out.println("\n----------------------------------------");
                                System.out.println("🎉 破解成功!");
                                System.out.println("密码: " + password);
                                System.out.println("哈希: " + hash);
                                System.out.println("尝试次数: " + current);
                                System.out.println("----------------------------------------");
                                executor.shutdownNow();
                                return;
                            }
                        } catch (Exception e) {
                            invalidPasswords.incrementAndGet();
                            if (!(e instanceof java.security.spec.InvalidKeySpecException)) {
                                System.out.println("\n处理密码时出错: " + e.getMessage());
                            }
                        }
                    }
                }));
            }

            try {
                for (Future<?> future : futures) {
                    future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                if (!(e.getCause() instanceof java.security.spec.InvalidKeySpecException)) {
                    System.out.println("\n执行出错: " + e.getMessage());
                }
            }

            long endTime = System.currentTimeMillis();
            if (!found.get()) {
                System.out.printf("\n未找到匹配的密码，耗时: %.2f秒\n", (endTime - startTime) / 1000.0);
                System.out.println("尝试的总密码数: " + totalLines);
                System.out.println("无效的密码数: " + invalidPasswords.get());
            }

        } catch (IOException e) {
            System.out.println("读取字典文件失败: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}