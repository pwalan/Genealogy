package com.github.pwalan.genealogy.utils;

/**
 * 成员信息类
 */
public class Member {
    private String name;
    private String gender;
    private String partner;
    private String father;
    private String mother;
    private int x;
    private int y;

    public Member() {
    }

    public Member(String name, String gender, String partner, String father, String mother, int x, int y) {
        super();
        this.name = name;
        this.gender = gender;
        this.partner = partner;
        this.father = father;
        this.mother = mother;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getFather() {
        return father;
    }

    public void setFather(String father) {
        this.father = father;
    }

    public String getMother() {
        return mother;
    }

    public void setMother(String mother) {
        this.mother = mother;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }


}
