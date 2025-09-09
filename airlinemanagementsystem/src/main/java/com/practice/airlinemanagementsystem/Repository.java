package com.practice.airlinemanagementsystem;

import java.util.List;
import java.util.Optional;

public interface Repository<T,ID> {
    T save(T t);
    Optional<T> findbyId(ID id);
    List<T> findAll();
    void deleteById(ID id);
}
