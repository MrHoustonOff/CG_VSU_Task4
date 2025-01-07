package com.cgvsu;

import com.cgvsu.math.Vector3f;

public class Main {
    public static void main(String[] args) {
        Simple3DViewer.main(args);

       Vector3f testVector = new Vector3f(0,1,1);
        Vector3f a = testVector.add(new Vector3f(1,1,1));
        System.out.println(a);
    }
}
