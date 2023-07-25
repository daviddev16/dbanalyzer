package com.daviddev16;

public class RowData {

    private String content;
    private int occurrences;
    private String tableName;
    private String ctid;

    public RowData(String content, int occurrences, String tableName, String ctid) {
        this.content = content;
        this.occurrences = occurrences;
        this.tableName = tableName;
        this.ctid = ctid;
    }

    public RowData() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCtid() {
        return ctid;
    }

    public void setCtid(String ctid) {
        this.ctid = ctid;
    }
}
