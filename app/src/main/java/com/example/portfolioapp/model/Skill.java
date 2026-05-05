package com.example.portfolioapp.model;

import java.io.Serializable;

public class Skill implements Serializable {
    public String _id;
    public String name;
    public String category;
    public int    percentage;
    public String getId() { return _id != null ? _id : ""; }
}