package com.siongu.sucker.api;

public class Sucker {
    public static void suck(Object o) {
        ISucker target = null;
        try {
            target = (ISucker) Class.forName(o.getClass().getCanonicalName() + "$ViewSucking").newInstance();
            target.suck(o);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
