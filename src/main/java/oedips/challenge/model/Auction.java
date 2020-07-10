package oedips.challenge.model;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import java.util.stream.Collectors;
import java.time.ZonedDateTime;

import oedips.challenge.utils.JsonUtils;

/**
 * Model of an auction.
 */
public class Auction {

    private volatile List<Bid> bids = new CopyOnWriteArrayList<>();

    private final String name;
    private final String description;
    private final long startPrice;
    private final long startTime;
    private final long endTime;
    private String buyer;
    private AuctionLifeCycle status = AuctionLifeCycle.NOT_STARTED;

    /**
     * Creates an auction
     * 
     * @param name
     * @param description
     * @param endTime
     * @param startPrice
     */
    public Auction(final String name, final String description, final long endTime, final long startPrice) {
        this.name = name;
        this.description = description;
        this.startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        this.endTime = endTime;
        this.startPrice = startPrice;
    }

    /**
     * Adds a bid if bid is valid and if auction is running.
     * 
     * @param username
     * @param biddingValue
     * @return true if added
     */
    public boolean addBid(String username, long biddingValue) {
        if (biddingValue > computeCurrentPrice() && computeStatus() == AuctionLifeCycle.RUNNING) {
            Bid b = new Bid(username, biddingValue);
            this.bids.add(b);
            return true;
        }
        return false;
    }

    /**
     * Checks Time and updates auction status and winner.
     */
    public AuctionLifeCycle computeStatus() {

        if (this.status == AuctionLifeCycle.DELETED) {
            return this.status;
        }
        long time = ZonedDateTime.now().toInstant().toEpochMilli();
        if (time > startTime) {
            if (time < endTime) {
                this.status = AuctionLifeCycle.RUNNING;
            } else {
                this.status = AuctionLifeCycle.TERMINATED;
                Bid winningBid = computeWinningBid();
                if (winningBid != null) {
                    this.buyer = winningBid.getUsername();
                }
            }
        } else {
            this.status = AuctionLifeCycle.NOT_STARTED;
        }
        return this.status;
    }

    private long computeCurrentPrice() {
        if (bids.isEmpty()) {
            return this.startPrice;
        }
        return this.bids.get(bids.size() - 1).getValue();
    }

    private Bid computeWinningBid() {
        if (bids.isEmpty()) {
            return null;
        }
        return this.bids.get(bids.size() - 1);
    }

    /**
     * Returns the name of the Auction.
     * 
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns collection of bids
     * 
     * @return
     */
    public Collection<Bid> listBids() {
        return this.bids;
    }

    /**
     * Returns a filtered collection of bids.
     * 
     * @param username
     * @return
     */
    public Collection<Bid> listBids(String username) {
        if (username == null) {
            return this.bids;
        }
        return this.bids.stream().filter(e -> e.getUsername().equals(username)).collect(Collectors.toList());
    }

    /**
     * Updates the status of the auction and returns the winner.
     * 
     * @return
     */
    public String computeBuyer() {
        computeStatus();
        return this.buyer;
    }

    /**
     * Sets the status of the auction as DELETED.
     */
    public void setAsDeleted() {
        this.status = AuctionLifeCycle.DELETED;
    }

    /**
     * Returns the description of the Auction.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return JsonUtils.asJsonString(this);
    }
}
