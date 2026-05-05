package com.example.portfolioapp.model;

import java.io.Serializable;

public class Project implements Serializable {
    public String _id;
    public String title;
    public String description;
    public String techStack;
    public String githubUrl;
    public String imageUrl;
    public String imagePublicId;
    public boolean isExpanded = false;

    public String getId() { return _id != null ? _id : ""; }
}

//
//public class Project implements Serializable {
//    public String _id;       // MongoDB id
//    public String id;        // alias (same field)
//    public String title;
//    public String description;
//    public String techStack;
//    public String githubUrl;
//    public String imageUrl;
//    public String imagePublicId;
//}