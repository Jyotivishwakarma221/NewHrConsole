
package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;


public class Pageable {

    @SerializedName("offset")
    private Long mOffset;
    @SerializedName("pageNumber")
    private Long mPageNumber;
    @SerializedName("pageSize")
    private Long mPageSize;
    @SerializedName("paged")
    private Boolean mPaged;
    @SerializedName("sort")
    private Sort mSort;
    @SerializedName("unpaged")
    private Boolean mUnpaged;

    public Long getOffset() {
        return mOffset;
    }

    public void setOffset(Long offset) {
        mOffset = offset;
    }

    public Long getPageNumber() {
        return mPageNumber;
    }

    public void setPageNumber(Long pageNumber) {
        mPageNumber = pageNumber;
    }

    public Long getPageSize() {
        return mPageSize;
    }

    public void setPageSize(Long pageSize) {
        mPageSize = pageSize;
    }

    public Boolean getPaged() {
        return mPaged;
    }

    public void setPaged(Boolean paged) {
        mPaged = paged;
    }

    public Sort getSort() {
        return mSort;
    }

    public void setSort(Sort sort) {
        mSort = sort;
    }

    public Boolean getUnpaged() {
        return mUnpaged;
    }

    public void setUnpaged(Boolean unpaged) {
        mUnpaged = unpaged;
    }

}
