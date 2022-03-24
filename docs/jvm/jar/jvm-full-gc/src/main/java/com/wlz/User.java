package com.wlz;

/**
 * @author wlz
 * @date 2022-03-24  10:11 下午
 */
public class User {

    private int id;
    private String name;

    byte[] a = new byte[1024*100];

    public User(){}

    public User(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
