package com.example.portfolioapp.model;

public class Resume {
    public String _id;
    public String pdfUrl;
    public String pdfPublicId;
    public String originalName;  // ← this too
    public String message;

    // Keep these for backward compatibility if you had old fields
    public String viewUrl;
    public String downloadUrl;


    public String getId() {
        return _id != null ? _id : "";
    }

}