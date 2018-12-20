
package com.watshout.mobile.pojo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("multicast_ids")
    @Expose
    private List<Integer> multicastIds = null;
    @SerializedName("success")
    @Expose
    private Integer success;
    @SerializedName("failure")
    @Expose
    private Integer failure;
    @SerializedName("canonical_ids")
    @Expose
    private Integer canonicalIds;
    @SerializedName("results")
    @Expose
    private List<Result_> results = null;
    @SerializedName("topic_message_id")
    @Expose
    private Object topicMessageId;

    public List<Integer> getMulticastIds() {
        return multicastIds;
    }

    public void setMulticastIds(List<Integer> multicastIds) {
        this.multicastIds = multicastIds;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getFailure() {
        return failure;
    }

    public void setFailure(Integer failure) {
        this.failure = failure;
    }

    public Integer getCanonicalIds() {
        return canonicalIds;
    }

    public void setCanonicalIds(Integer canonicalIds) {
        this.canonicalIds = canonicalIds;
    }

    public List<Result_> getResults() {
        return results;
    }

    public void setResults(List<Result_> results) {
        this.results = results;
    }

    public Object getTopicMessageId() {
        return topicMessageId;
    }

    public void setTopicMessageId(Object topicMessageId) {
        this.topicMessageId = topicMessageId;
    }

}
