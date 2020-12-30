package com.mg.kafka.entity;

/**
 * @author whz
 * @create 2020-12-15 19:01
 * @desc TODO: add description here
 **/
public class Address {
    private String country;
    private String city;
    private String street;
    private String postCode;

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getPostCode() {
    return postCode;
  }

  public void setPostCode(String postCode) {
    this.postCode = postCode;
  }
}