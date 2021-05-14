package io.github.imsejin.mybatis.pagination.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.imsejin.mybatis.pagination.serializer.PaginatorSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.*;

/**
 * {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}의
 * 리턴 값이 {@link List}이기에 'polymorphism'이 적용될 수 있게 {@link List}를
 * 구현해야 한다.
 *
 * @param <T> type
 */
@Getter
@ToString
@EqualsAndHashCode
@JsonSerialize(using = PaginatorSerializer.class)
public class Paginator<T> implements List<T> {

    private final List<T> items;

    private final PageInfo pageInfo;

    public Paginator(List<T> items, PageInfo pageInfo) {
        this.items = items == null ? Collections.emptyList() : items;
        this.pageInfo = pageInfo;
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.items.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.items.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.items.toArray();
    }

    @Override
    @SuppressWarnings("SuspiciousToArrayCall")
    public <T1> T1[] toArray(T1[] a) {
        return this.items.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return this.items.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return this.items.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.items.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.items.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return this.items.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.items.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.items.retainAll(c);
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    public T get(int index) {
        return this.items.get(index);
    }

    @Override
    public T set(int index, T element) {
        return this.items.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.items.add(index, element);
    }

    @Override
    public T remove(int index) {
        return this.items.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.items.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.items.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return this.items.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return this.items.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return this.items.subList(fromIndex, toIndex);
    }

}
