package com.example.wifidemo1.Function;

/**
 * @author: fuhejian
 * @date: 2023/3/15
 */
public interface MyConsumer<T> {
    void accept(T t);
}
