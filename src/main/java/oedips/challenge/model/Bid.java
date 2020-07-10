package oedips.challenge.model;

import oedips.challenge.utils.JsonUtils;

/**
 * Model of a bidding.
 */
public class Bid {

    private final String username;
    private final long value;

    /**
     * Creates a bid.
     * 
     * @param username
     * @param value
     */
    public Bid(final String username, final long value) {
        this.username = username;
        this.value = value;
    }

    /**
     * Gets the value of the Bid.
     * 
     * @return
     */
    public long getValue() {
        return this.value;
    }

    /**
     * Gets the username of the bid.
     * 
     * @return
     */
    public String getUsername() {
        return this.username;
    }

    @Override
    public String toString() {
        return JsonUtils.asJsonString(this);
    }

}
