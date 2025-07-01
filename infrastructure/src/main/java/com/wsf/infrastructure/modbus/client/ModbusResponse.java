package com.wsf.infrastructure.modbus.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModbusResponse {
    private final int transactionId;
    private final int unitId;
    private final int functionCode;
    private final byte[] data;
    private final boolean isError;
    private final int errorCode;

    // 获取寄存器值数组
    public int[] getRegisterValues() {
        if (isError || data == null || data.length < 2) {
            return new int[0];
        }

        int byteCount = data[0] & 0xFF;
        int registerCount = byteCount / 2;
        int[] values = new int[registerCount];

        for (int i = 0; i < registerCount; i++) {
            int highByte = data[1 + i * 2] & 0xFF;
            int lowByte = data[2 + i * 2] & 0xFF;
            values[i] = (highByte << 8) | lowByte;
        }

        return values;
    }
}
