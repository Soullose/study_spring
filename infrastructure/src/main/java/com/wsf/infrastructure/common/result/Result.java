package com.wsf.infrastructure.common.result;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class Result<T> implements Serializable {

	private int status;

	private String code;

	private T data;

	private String msg;

	public static <T> Result<T> success() {
		return success(null);
	}

	public static <T> Result<T> success(T data) {
		Result<T> result = new Result<>();
		result.setStatus(HttpStatus.OK.value());
		result.setCode(ResultCode.SUCCESS.getCode());
		result.setMsg(ResultCode.SUCCESS.getMsg());
		result.setData(data);
		return result;
	}

	public static <T> Result<T> failed() {
		return result(HttpStatus.INTERNAL_SERVER_ERROR.value(), ResultCode.SYSTEM_ERROR.getCode(),
				ResultCode.SYSTEM_ERROR.getMsg(), null);
	}

	public static <T> Result<T> failed(String msg) {
		return result(HttpStatus.INTERNAL_SERVER_ERROR.value(), ResultCode.SYSTEM_ERROR.getCode(), msg, null);
	}

	public static <T> Result<T> judge(boolean status) {
		if (status) {
			return success();
		} else {
			return failed();
		}
	}

	public static <T> Result<T> failed(IResultCode resultCode) {
		return result(HttpStatus.INTERNAL_SERVER_ERROR.value(), resultCode.getCode(), resultCode.getMsg(), null);
	}

	public static <T> Result<T> failed(int status, IResultCode resultCode) {
		return result(status, resultCode.getCode(), resultCode.getMsg(), null);
	}
	public static <T> Result<T> failed(IResultCode resultCode, String msg) {
		return result(HttpStatus.INTERNAL_SERVER_ERROR.value(), resultCode.getCode(),
				StringUtils.isNotBlank(msg) ? msg : resultCode.getMsg(), null);
	}
	public static <T> Result<T> failed(int status, IResultCode resultCode, String msg) {
		return result(status, resultCode.getCode(), StringUtils.isNotBlank(msg) ? msg : resultCode.getMsg(), null);
	}

	private static <T> Result<T> result(int status, IResultCode resultCode, T data) {
		return result(status, resultCode.getCode(), resultCode.getMsg(), data);
	}

	private static <T> Result<T> result(int status, String code, String msg, T data) {
		Result<T> result = new Result<>();
		result.setStatus(status);
		result.setCode(code);
		result.setData(data);
		result.setMsg(msg);
		return result;
	}

	public static boolean isSuccess(Result<?> result) {
		return result != null && ResultCode.SUCCESS.getCode().equals(result.getCode());
	}
}
