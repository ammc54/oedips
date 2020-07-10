package oedips.challenge.ws;

import javax.ws.rs.GET;

import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;

import oedips.challenge.model.Auction;
import oedips.challenge.model.AuctionHouse;
import oedips.challenge.model.AuctionLifeCycle;
import oedips.challenge.model.ErrorMessage;
import oedips.challenge.model.Bid;

/**
 * Ressource /house
 * 
 * Manages an auction house. Supported operations :
 * <li>creation/deletion of auction house
 * <li>creation/deletion of auction
 * <li>creation of bids
 * <li>lists auction houses/auctions/bids
 * 
 */
@Path("/house")
public class HouseResource {

    public static volatile ConcurrentHashMap<String, AuctionHouse> houses = new ConcurrentHashMap<>();

    private static final String houseNotFoundError = (new ErrorMessage("House does not exist")).toString();
    private static final String auctionNotFoundError = (new ErrorMessage("Auction does not exist")).toString();

    private static final String HouseAlreadyExistsError = (new ErrorMessage("House already exists")).toString();
    private static final String AuctionAlreadyExistsError = (new ErrorMessage("auction already exists")).toString();

    private static final String AuctionNotTerminated = (new ErrorMessage("Auction Not terminated")).toString();

    private static final String BidNotValidError = (new ErrorMessage("Bid not valid")).toString();

    /**
     * Lists all action houses
     * 
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)

    public Response getAuctionHouse() {
        return Response.ok(houses.values()).build();
    }

    /**
     * Deletes a specific house
     * 
     * @PathParam houseName
     * @return This call returns :
     * 
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 404 if house does not exist
     * 
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}")

    public Response deleteAuctionHouse(@PathParam("houseName") final String houseName) {
        if (houses.get(houseName) == null) {
            return Response.status(404).entity(houseNotFoundError).build();
        }
        houses.remove(houseName);
        return Response.ok("{\"Message\": \"house deleted\"}").build();
    }

    /**
     * Creates an auction house.
     * 
     * @PathParam houseName (String)
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 409 if house already exists
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}")

    public Response postAuctionHouse(@PathParam("houseName") final String houseName) {

        if (houses.get(houseName) != null) {
            return Response.status(409).entity(HouseAlreadyExistsError).build();
        }
        houses.put(houseName, new AuctionHouse(houseName));
        return Response.ok("{\"Message\": \"house "+houseName+" created\"}").build();
    }

    /**
     * Lists auctions for a specific house and optionally for a specific user. Can
     * optionally be filtered by status : NOT_STARTED, RUNNING, TERMINATED, DELETED;
     * 
     * @PathParam houseName (String)
     * @QueryParam username (String)
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 404 if house does not exist
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}/auction")

    public Response listAuctions(@PathParam("houseName") final String houseName,
            @PathParam("auctionName") final String auctionName, @QueryParam("status") final String status) {
        AuctionHouse house = houses.get(houseName);
        if (house == null) {
            return Response.status(404).entity(houseNotFoundError).build();
        }
        return Response.ok(house.listAuctions(status)).build();
    }

    /**
     * Marks a specific auction as being deleted.
     * 
     * @PathParam houseName (String)
     * @QueryParam username (String)
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 404 if house does not exist
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}/auction/{auctionName}")

    public Response deleteAuction(@PathParam("houseName") final String houseName,
            @PathParam("auctionName") final String auctionName) {
        AuctionHouse house = houses.get(houseName);
        if (house == null) {
            return Response.status(404).entity(houseNotFoundError).build();
        }
        Auction auction = house.getAuction(auctionName);
        if (auction == null) {
            return Response.status(404).build();
        }
        auction.setAsDeleted();
        return Response.ok("{\"Message\": \"auction deleted\"}").build();
    }

    /**
     * Creates an auction for a specific house.
     * 
     * @PathParam houseName (String)
     * @PathParam auctionName (String)
     * @QueryParam dsc description of the auction (String) - defaults to null
     * @QueryParam startTime start time of the auction (long) - defaults to 0
     * @QueryParam endTime end time of the auction (long) - defaults to 0
     * @QueryParam startPrice starting price of the auction (long) - defaults to 0
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 404 if house does not exist
     *         <li>409 if auction already exists
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}/auction/{auctionName}")

    public Response postAuction(@PathParam("houseName") final String houseName,
            @PathParam("auctionName") final String auctionName, @QueryParam("dsc") final String description,
            @QueryParam("startTime") final long startTime, @QueryParam("endTime") final long endTime,
            @QueryParam("startPrice") final long startPrice) {

        AuctionHouse house = houses.get(houseName);
        if (house == null) {
            return Response.status(404).entity(houseNotFoundError).build();
        }
        if (house.addAuction(new Auction(auctionName, description, endTime, startPrice)) == false) {
            return Response.status(409).entity(AuctionAlreadyExistsError).build();
        }

        return Response.ok("{\"Message\": \"auction "+auctionName+" created\"}").build();

    }

    /**
     * Lists all bids for a specific house and auction, and optionally for a user.
     * 
     * @PathParam houseName (String)
     * @PathParam auctionName (String)
     * @QueryParam username username (String)
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 404 if house or auction does not exist
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}/auction/{auctionName}/bid")

    public Response listBids(@PathParam("houseName") final String houseName,
            @PathParam("auctionName") final String auctionName, @QueryParam("username") final String username) {

        AuctionHouse house = houses.get(houseName);
        if (house == null) {
            return Response.status(404).entity(houseNotFoundError).build();
        }
        Auction auction = house.getAuction(auctionName);
        if (auction == null) {
            return Response.status(404).entity(auctionNotFoundError).build();
        }

        return Response.ok(auction.listBids(username)).build();
    }

    /**
     * Returns the winner of the auction.
     * 
     * @param houseName
     * @param auctionName
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 404 if house or auction does not exist
     *         <li>HTTP 409 if auction not terminated
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}/auction/{auctionName}/winner")

    public Response getWinner(@PathParam("houseName") final String houseName,
            @PathParam("auctionName") final String auctionName) {
        AuctionHouse house = houses.get(houseName);
        if (house == null) {
            return Response.status(404).entity(houseNotFoundError).build();
        }
        Auction auction = house.getAuction(auctionName);
        if (auction.computeStatus() != AuctionLifeCycle.TERMINATED) {
            return Response.status(409).entity(AuctionNotTerminated).build();
        }
        return Response.ok(auction.computeBuyer()).build();
    }

    /**
     * Creates a bid for a specific house and auction and user.
     * 
     * @PathParam houseName (String)
     * @PathParam auctionName (String)
     * @PathParam username username (String)
     * @bid bid bidding value (long) - defaults to 0
     * @return This call returns :
     *         <li>HTTP 200 if call successful
     *         <li>HTTP 404 if house or auction does not exist
     *         <li>422 if bid not valid, i.e. bidding value has already been
     *         outbidded, or auction not running.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{houseName}/auction/{auctionName}/bid/{username}")

    public Response postBid(@PathParam("houseName") final String houseName,
            @PathParam("auctionName") final String auctionName, @PathParam("username") final String username,
            @QueryParam("bid") final long biddingValue) {

        final AuctionHouse auctionHouse = houses.get(houseName);
        if (auctionHouse == null) {
            return Response.status(404).entity(houseNotFoundError).build();
        }

        if (auctionHouse.getAuction(auctionName) == null) {
            return Response.status(404).entity(auctionNotFoundError).build();

        }

        boolean added = houses.get(houseName).addBid(auctionName, username, biddingValue);
        if (!added) {
            return Response.status(422).entity(BidNotValidError).build();
        }
        return Response.ok("{\"Message\": \"bid created\"}").build();

    }

}
