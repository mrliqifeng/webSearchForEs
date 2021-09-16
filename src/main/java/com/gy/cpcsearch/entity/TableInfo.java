package com.gy.cpcsearch.entity;

public class TableInfo {
    private String id;

    private String tName;

    private String tStatus;

    private String tAlias;

    private String tType;

    private String tOfficalTable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName == null ? null : tName.trim();
    }

    public String gettStatus() {
        return tStatus;
    }

    public void settStatus(String tStatus) {
        this.tStatus = tStatus == null ? null : tStatus.trim();
    }

    public String gettAlias() {
        return tAlias;
    }

    public void settAlias(String tAlias) {
        this.tAlias = tAlias == null ? null : tAlias.trim();
    }

    public String gettType() {
        return tType;
    }

    public void settType(String tType) {
        this.tType = tType == null ? null : tType.trim();
    }

    public String gettOfficalTable() {
        return tOfficalTable;
    }

    public void settOfficalTable(String tOfficalTable) {
        this.tOfficalTable = tOfficalTable == null ? null : tOfficalTable.trim();
    }
}