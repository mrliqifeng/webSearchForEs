package com.gy.cpcsearch.entity;

public class AggInfo {
    private Integer id;

    private String aTable;

    private String aField;

    private String aAlias;

    private Integer aLength;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getaTable() {
        return aTable;
    }

    public void setaTable(String aTable) {
        this.aTable = aTable == null ? null : aTable.trim();
    }

    public String getaField() {
        return aField;
    }

    public void setaField(String aField) {
        this.aField = aField == null ? null : aField.trim();
    }

    public String getaAlias() {
        return aAlias;
    }

    public void setaAlias(String aAlias) {
        this.aAlias = aAlias == null ? null : aAlias.trim();
    }

    public Integer getaLength() {
        return aLength;
    }

    public void setaLength(Integer aLength) {
        this.aLength = aLength;
    }
}