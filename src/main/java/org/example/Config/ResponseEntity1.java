package org.example.Config;

public class ResponseEntity1<T> {
    private T data;
    private int statusCode;
    private String message;

    public ResponseEntity1(T data, int statusCode, String message) {
        this.data = data;
        this.statusCode = statusCode;
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
