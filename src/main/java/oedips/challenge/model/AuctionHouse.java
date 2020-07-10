package oedips.challenge.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import oedips.challenge.utils.JsonUtils;

/**
 * Model of an auction house.
 */
public class AuctionHouse {

    private final String name;

    private volatile Map<String, Auction> auctions = new ConcurrentHashMap<>();

    /**
     * Creates an AuctionHouse.
     * 
     * @param name
     */
    public AuctionHouse(final String name) {
        this.name = name;
    }

    /**
     * Adds an auction if it does not exist.
     * 
     * @param auction
     * @return true if auction was added.
     */
    public boolean addAuction(Auction auction) {
        if (this.auctions.get(auction.getName()) != null) {
            return false;
        }
        this.auctions.put(auction.getName(), auction);
        return true;
    }

    /**
     * Adds a bid if valid : auction is started and bid higher than current price.
     * 
     * @param auctionName
     * @param username
     * @param biddingValue
     * @return true if bid was added.
     */
    public boolean addBid(String auctionName, String username, long biddingValue) {
        Auction a = this.auctions.get(auctionName);
        if (a != null) {
            if (a.addBid(username, biddingValue)) {
                addAuction(a);
                return true;
            }
        }
        return false;
    }

    /**
     * Lists the auctions for the house. If status input is valid, lists the
     * auctions with the desired status.
     * 
     * @param status
     * @return
     */
    public List<Auction> listAuctions(String status) {
        if (status == null) {
            return new ArrayList<>(auctions.values());
        }
        try {
            AuctionLifeCycle auctionStatus = AuctionLifeCycle.valueOf(status);
            return auctions.values().stream().filter(e -> e.computeStatus().equals(auctionStatus))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IllegalArgumentException e) {
            return new ArrayList<>(auctions.values());
        }
    }

    /**
     * Returns a specific auction
     * 
     * @param auctionName
     * @return the auction, null if does not exist
     */
    public Auction getAuction(String auctionName) {
        return this.auctions.get(auctionName);
    }

    /**
     * Returns the name of the auction house.
     * 
     * @return
     */
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return JsonUtils.asJsonString(this);
    }

}
