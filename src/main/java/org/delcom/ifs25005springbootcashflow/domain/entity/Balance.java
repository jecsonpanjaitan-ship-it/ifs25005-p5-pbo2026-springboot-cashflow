package org.delcom.ifs25005springbootcashflow.domain.entity;

/** Ringkasan keuangan seorang pengguna. */
public record Balance(int count, long income, long expense) {

    public long total() {
        return income - expense;
    }
}
