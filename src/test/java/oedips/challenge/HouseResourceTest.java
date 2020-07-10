package s;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import oedips.challenge.model.AuctionHouse;
import oedips.challenge.model.Bid;
import oedips.challenge.model.Auction;
import oedips.challenge.ws.HouseResource;

public class HouseResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return new ResourceConfig(HouseResource.class);
    }

    @After
    public void tearDown() {
        deleteAuctionHouse("h1");
        deleteAuctionHouse("h2");
        deleteAuctionHouse("h3");
        deleteAuctionHouse("h4");
    }

    public Response createAuctionHouse(String houseName) {
        String target = String.format("house/%s", houseName);
        System.out.println("POST " + target);
        return target(target).request().post(null);
    }

    private Response createAuction(String houseName, String auctionName, String description, long endTime,
            long startPrice) {
        String target = String.format("house/%s/auction/%s", houseName, auctionName);
        System.out.println("POST " + target);
        return target(target).queryParam("dsc", description).queryParam("endTime", endTime)
                .queryParam("startPrice", startPrice).request().post(null);
    }

    private Response createBid(String houseName, String auctionName, String username, long biddingValue) {
        String target = String.format("house/%s/auction/%s/bid/%s", houseName, auctionName, username);
        System.out.println("POST " + target);
        return target(target).queryParam("bid", biddingValue).request().post(null);
    }

    private Builder getAuctionHouses() {
        String target = String.format("house");
        System.out.println("GET " + target);
        return target(target).request();
    }

    private Builder getAuctions(String houseName) {
        String target = String.format("house/%s/auction", houseName);
        System.out.println("GET " + target);
        return target(target).request();
    }

    private Builder getBids(String houseName, String auctionName, String username) {
        String target = String.format("house/%s/auction/%s/bid", houseName, auctionName);
        System.out.println("GET " + target);
        return target(target).queryParam("username", username).request();
    }

    private Builder getWinner(String houseName, String auctionName) {
        String target = String.format("house/%s/auction/%s/winner", houseName, auctionName);
        System.out.println("GET " + target);
        return target(target).request();
    }

    private Response deleteAuctionHouse(String houseName) {
        String target = String.format("house/%s", houseName);
        System.out.println("DELETE " + target);
        return target(target).request().delete();
    }

    private Response deleteAuction(String houseName, String auctionName) {
        String target = String.format("house/%s/auction/%s", houseName, auctionName);
        System.out.println("DELETE " + target);
        return target(target).request().delete();
    }

    @Test
    public void testGetResources()
            throws InterruptedException, JsonMappingException, JsonProcessingException, ParseException {

        String houseName = "h2";
        String auctionName = "a2";
        String description = "d2";
        String username = "u2";

        long endTime = ZonedDateTime.now().plusYears(30).toInstant().toEpochMilli();

        createAuctionHouse(houseName);
        createAuction(houseName, auctionName, description, endTime, 1);
        createBid(houseName, auctionName, username, 1000);

        ObjectMapper mapper = new ObjectMapper();

        List<AuctionHouse> houses = mapper.readValue(getAuctionHouses().get(String.class), List.class);
        assertEquals(1, houses.size());

        List<Auction> auctions = mapper.readValue(getAuctions(houseName).get(String.class), List.class);
        assertEquals(1, auctions.size());

        List<Bid> bids = mapper.readValue(getBids(houseName, auctionName, username).get(String.class), List.class);
        assertEquals(1, bids.size());

        createBid(houseName, auctionName, username, 1001);
        List<Bid> bids2 = mapper.readValue(getBids(houseName, auctionName, username).get(String.class), List.class);
        assertEquals(2, bids2.size());

        createBid(houseName, auctionName, username, 10);
        List<Bid> bids3 = mapper.readValue(getBids(houseName, auctionName, username).get(String.class), List.class);
        assertEquals(2, bids3.size());

    }

    @Test
    public void testPostResources() throws InterruptedException, ParseException {

        String houseName = "h1";
        String auctionName = "a1";
        String description = "d1";
        String username = "u1";

        long endTime = ZonedDateTime.now().plusYears(30).toInstant().toEpochMilli();

        Response houseCreateResponse = createAuctionHouse(houseName);
        Assert.assertEquals(200, houseCreateResponse.getStatus());

        Response houseCreateResponseBis = createAuctionHouse(houseName);
        Assert.assertEquals(409, houseCreateResponseBis.getStatus());

        Response auctionCreateResponse = createAuction(houseName, auctionName, description, endTime, 1);
        Assert.assertEquals(200, auctionCreateResponse.getStatus());

        Response auctionCreateResponseBis = createAuction(houseName, auctionName, description, endTime, 1);
        Assert.assertEquals(409, auctionCreateResponseBis.getStatus());

        Response auctionCreateResponseKO = createAuction("non-existing-house", auctionName, description, endTime, 1);
        Assert.assertEquals(404, auctionCreateResponseKO.getStatus());

        Response bidCreateResponse = createBid(houseName, auctionName, username, 1000);
        Assert.assertEquals(200, bidCreateResponse.getStatus());

        Response bidCreateResponseKO = createBid(houseName, "non-existing-auction", username, 1000);
        Assert.assertEquals(404, bidCreateResponseKO.getStatus());

        Response bidCreateResponseKO2 = createBid("non-existing-house", auctionName, username, 1000);
        Assert.assertEquals(404, bidCreateResponseKO2.getStatus());

    }

    @Test
    public void testDeleteResources() throws InterruptedException, ParseException {

        String houseName = "h3";
        String auctionName = "a3";
        String description = "d3";

        long endTime = ZonedDateTime.now().plusYears(30).toInstant().toEpochMilli();

        createAuctionHouse(houseName);
        createAuction(houseName, auctionName, description, endTime, 1);

        Response nonExistingAuctionDeleteResponse1 = deleteAuction("non-existing-houseName", auctionName);
        Assert.assertEquals(404, nonExistingAuctionDeleteResponse1.getStatus());

        Response nonExistingAuctionDeleteResponse2 = deleteAuction(houseName, "non-existing-auctionName");
        Assert.assertEquals(404, nonExistingAuctionDeleteResponse2.getStatus());

        Response auctionDeleteResponse = deleteAuction(houseName, auctionName);
        Assert.assertEquals(200, auctionDeleteResponse.getStatus());

        Response nonExistingHouseDeleteResponse = deleteAuctionHouse("non-existing-houseName");
        Assert.assertEquals(404, nonExistingHouseDeleteResponse.getStatus());

        Response houseDeleteResponse = deleteAuctionHouse(houseName);
        Assert.assertEquals(200, houseDeleteResponse.getStatus());

    }

    @Test
    public void testGetBuyer() throws InterruptedException, ParseException {

        String houseName = "h4";
        String auctionName = "a4";
        String description = "d4";

        long endTime = ZonedDateTime.now().toInstant().toEpochMilli();
        createAuctionHouse(houseName);
        createAuction(houseName, auctionName, description, (endTime + 1000l), 1);

        createBid(houseName, auctionName, "u1", 1000);
        createBid(houseName, auctionName, "u2", 3000);
        createBid(houseName, auctionName, "u3", 2000);
        Thread.sleep(1200);

        createBid(houseName, auctionName, "u3", 4000);

        String buyer = getWinner(houseName, auctionName).get(String.class);
        assertEquals("u2", buyer);

    }
}
