package org.jsoup.helper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class DescendableLinkedList<E> extends LinkedList<E> {
    public DescendableLinkedList() {
        super();
    }
    public void push(E e) {
        addFirst(e);
    }
    public E peekLast() {
        return size() == 0 ? null : getLast();
    }
    public E pollLast() {
        return size() == 0 ? null : removeLast();
    }
    public Iterator<E> descendingIterator() {
        return new DescendingIterator<E>(size());
    }

    private class DescendingIterator<E> implements Iterator<E> {
        private final ListIterator<E> iter;
        private DescendingIterator(int index) {
            iter = (ListIterator<E>) listIterator(index);
        }
        public boolean hasNext() {
            return iter.hasPrevious();
        }
        public E next() {
            return iter.previous();
        }
        public void remove() {
            iter.remove();
        }
    }

}