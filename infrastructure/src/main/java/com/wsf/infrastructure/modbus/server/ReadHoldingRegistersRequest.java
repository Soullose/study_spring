package com.wsf.infrastructure.modbus.server;

import lombok.Data;

@Data
public class ReadHoldingRegistersRequest implements ModbusRequest{
    private final int startAddress;
    private final int quantity;

    public ReadHoldingRegistersRequest(int startAddress, int quantity) {
        this.startAddress = startAddress;
        this.quantity = quantity;
    }
}
