package com.olcayergun.btAracTespit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CircularCollectionWithLimit {

    public static <T, C extends Collection<T>> void addWithLimit(C c, T itemToAdd, int limit) {
        List<T> list = new ArrayList<>(c);
        list.add(itemToAdd);
        while (list.size() > limit) {
            list.remove(0);
        }
        c.clear();
        c.addAll(list);
    }

    public static void main(String[] args) {
        int limit = 4;
        Set<String> l = new HashSet<String>(4);
        System.out.println("given LinkedHashSet: " + l);
        addWithLimit(l, "6", limit);
        System.out.println(l);
        addWithLimit(l, "7", limit);
        System.out.println(l);
        addWithLimit(l, "8", limit);
        System.out.println(l);
        addWithLimit(l, "9", limit);
        System.out.println(l);
        addWithLimit(l, "0", limit);
        System.out.println(l);
        addWithLimit(l, "ol", limit);
        System.out.println(l);
        addWithLimit(l, "erg", limit);
        System.out.println(l);
    }
}