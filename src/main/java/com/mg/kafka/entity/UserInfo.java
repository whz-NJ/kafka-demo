package com.mg.kafka.entity;

/**
 * @author whz
 * @create 2020-12-15 19:01
 * @desc TODO: add description here
 **/
public class UserInfo {
    private String name;
    private Address address;
    private Integer credit;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Integer getCredit() {
    return credit;
  }

  public void setCredit(Integer credit) {
    this.credit = credit;
  }
}