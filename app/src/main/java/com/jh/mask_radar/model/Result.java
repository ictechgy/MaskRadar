package com.jh.mask_radar.model;

public class Result {
    private Result(){}

    static class Success<T>{
        private T data;
        public Success(T data){
            this.data = data;
        }
        T getData(){ return this.data; }
    }
    static class Error{
        private Exception error;
        public Error(Exception error){
            this.error = error;
        }
        Exception getError(){ return this.error; }
    }
}
