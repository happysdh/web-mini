package com.xunao.testlib;

public class ToJSBean {
    private String status;
    private String msg;
    private Object data;

    public ToJSBean(boolean success, Object data) {
        this.data = data;
        this.status = success ? "1" : "0";
        if(success) {
            this.msg = "成功";
        } else {
            if(data instanceof String) {
                this.msg = (String) data;
            } else {
                this.msg = "失败";
            }
        }

        this.msg = success ? "成功" : (String) data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
