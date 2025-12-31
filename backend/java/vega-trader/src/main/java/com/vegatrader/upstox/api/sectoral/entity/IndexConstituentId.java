package com.vegatrader.upstox.api.sectoral.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for IndexConstituentEntity.
 */
public class IndexConstituentId implements Serializable {
    private String indexCode;
    private String instrumentKey;

    public IndexConstituentId() {
    }

    public IndexConstituentId(String indexCode, String instrumentKey) {
        this.indexCode = indexCode;
        this.instrumentKey = instrumentKey;
    }

    public String getIndexCode() {
        return indexCode;
    }

    public void setIndexCode(String indexCode) {
        this.indexCode = indexCode;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IndexConstituentId that = (IndexConstituentId) o;
        return Objects.equals(indexCode, that.indexCode) && Objects.equals(instrumentKey, that.instrumentKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexCode, instrumentKey);
    }
}
