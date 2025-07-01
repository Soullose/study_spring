package com.wsf.infrastructure.modbus.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModbusRequest {
    private final int transactionId;
    private final int unitId;
    private final int functionCode;
    private final int startAddress;
    private final int quantity;
}
