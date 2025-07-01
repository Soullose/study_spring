package com.wsf.infrastructure.modbus.server;

import lombok.Data;

@Data
public class ReadHoldingRegistersResponse implements ModbusResponse{
    private final byte[] data;

    public ReadHoldingRegistersResponse() {
        // 示例：生成模拟数据（实际需从设备获取）
        this.data = new byte[]{0x01, 0x02, 0x03, 0x04}; // 模拟 4 个寄存器的值
    }

    public byte[] getData() {
        return data;
    }
}
