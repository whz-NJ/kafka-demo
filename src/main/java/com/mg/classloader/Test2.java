package com.mg.classloader;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

class Test2
{
    public  void g(){
        gg();
    }
    @CallerSensitive
    public  void gg(){
//        System.out.println("-1 : "+Reflection.getCallerClass(-1));
//        System.out.println("0 : "+Reflection.getCallerClass(0));
//        System.out.println("1 : "+Reflection.getCallerClass(1));
//        System.out.println("2 : "+Reflection.getCallerClass(2));
//        System.out.println("3 : "+Reflection.getCallerClass(3));
//        System.out.println("4 : "+Reflection.getCallerClass(4));
//        System.out.println("5 : "+Reflection.getCallerClass(5));
        System.out.println("empty: " + Reflection.getCallerClass());
    }
 
}