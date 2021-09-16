package com.gy.cpcsearch.entity;

public class FieldInfo {
    private String id;

    private String fTableName;

    private String fName;

    private String fDes;

    private Integer fFieldType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getfTableName() {
        return fTableName;
    }

    public void setfTableName(String fTableName) {
        this.fTableName = fTableName == null ? null : fTableName.trim();
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName == null ? null : fName.trim();
    }

    public String getfDes() {
        return fDes;
    }

    public void setfDes(String fDes) {
        this.fDes = fDes == null ? null : fDes.trim();
    }

    public Integer getfFieldType() {
        return fFieldType;
    }

    public void setfFieldType(Integer fFieldType) {
        this.fFieldType = fFieldType;
    }
}