package io.github.newhoo.mysql.explain;

/**
 * ExplainResultVo
 *
 * @author huzunrong
 * @since 1.0
 */
public class ExplainResultVo {

    private String id;
    private String selectType;
    private String table;
    private String partitions;
    private String type;
    private String possibleKeys;
    private String key;
    private String keyLen;
    private String ref;
    private String rows;
    private String filtered;
    private String extra;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelectType() {
        return selectType;
    }

    public void setSelectType(String selectType) {
        this.selectType = selectType;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(String partitions) {
        this.partitions = partitions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPossibleKeys() {
        return possibleKeys;
    }

    public void setPossibleKeys(String possibleKeys) {
        this.possibleKeys = possibleKeys;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyLen() {
        return keyLen;
    }

    public void setKeyLen(String keyLen) {
        this.keyLen = keyLen;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getRows() {
        return rows;
    }

    public void setRows(String rows) {
        this.rows = rows;
    }

    public String getFiltered() {
        return filtered;
    }

    public void setFiltered(String filtered) {
        this.filtered = filtered;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}