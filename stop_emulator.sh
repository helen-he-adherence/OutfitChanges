#!/bin/bash
# 停止 Android 模拟器进程的脚本

echo "正在查找 Android 模拟器进程..."

# 查找并关闭进程 1041
if ps -p 1041 > /dev/null 2>&1; then
    echo "找到进程 1041，正在关闭..."
    kill 1041
    sleep 1
    
    # 如果进程还在运行，强制关闭
    if ps -p 1041 > /dev/null 2>&1; then
        echo "进程仍在运行，强制关闭..."
        kill -9 1041
    else
        echo "进程已成功关闭"
    fi
else
    echo "进程 1041 未找到"
fi

# 查找所有 qemu 和 emulator 进程
echo ""
echo "查找所有模拟器相关进程..."
QEMU_PIDS=$(pgrep -f qemu)
EMULATOR_PIDS=$(pgrep -f emulator)

if [ -n "$QEMU_PIDS" ]; then
    echo "找到 qemu 进程: $QEMU_PIDS"
    echo "$QEMU_PIDS" | xargs kill 2>/dev/null
fi

if [ -n "$EMULATOR_PIDS" ]; then
    echo "找到 emulator 进程: $EMULATOR_PIDS"
    echo "$EMULATOR_PIDS" | xargs kill 2>/dev/null
fi

echo ""
echo "完成！"

