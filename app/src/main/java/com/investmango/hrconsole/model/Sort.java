
package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;

public class Sort {

    @SerializedName("empty")
    private Boolean mEmpty;
    @SerializedName("sorted")
    private Boolean mSorted;
    @SerializedName("unsorted")
    private Boolean mUnsorted;

    public Boolean getEmpty() {
        return mEmpty;
    }

    public void setEmpty(Boolean empty) {
        mEmpty = empty;
    }

    public Boolean getSorted() {
        return mSorted;
    }

    public void setSorted(Boolean sorted) {
        mSorted = sorted;
    }

    public Boolean getUnsorted() {
        return mUnsorted;
    }

    public void setUnsorted(Boolean unsorted) {
        mUnsorted = unsorted;
    }

}
