package com.practice.airlinemanagementsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class InMemoryRepository<T,ID> implements Repository<T,ID> {

    protected final Map<ID,T> store = new ConcurrentHashMap<>();
    private final Function<T,ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public T save(T t) {
        ID id = idExtractor.apply(t);
        store.put(id, t);
        return t;
    }

    @Override
    public Optional<T> findbyId(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(ID id) {
        store.remove(id);
    }
}
