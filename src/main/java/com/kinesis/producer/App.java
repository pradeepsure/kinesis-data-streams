package com.kinesis.producer;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kinesis.aws.AwsKinesisClient;
import com.kinesis.model.Order;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Hello world
 *
 */
public class App 
{
    List<String> productList = new ArrayList<>();

    Random random = new Random();
    public static void main( String[] args ) throws InterruptedException {
        App app = new App();
        app.populateProductList();
        //1. get client
        AmazonKinesis kinesisClient = AwsKinesisClient.getKinesisClient();
        for(int i=1;i<=10;i++){
            app.sendData(kinesisClient);
            System.out.println(i+ " data Sent...");
            Thread.sleep(500);
        }

    }

    private void sendData(AmazonKinesis kinesisClient){
        //2. PutRecordRequest
        PutRecordsRequest recordsRequest = new PutRecordsRequest();
        recordsRequest.setStreamName("OrderStream");
        recordsRequest.setRecords(getRecordsRequestList());

        //3. putRecord or putRecords - 500 records with single API call
        PutRecordsResult results = kinesisClient.putRecords(recordsRequest);
        if(results.getFailedRecordCount() > 0){
            System.out.println("Error occurred for records " + results.getFailedRecordCount());
        } else {
            System.out.println("Data sent successfully...");
        }

    }

    private List<PutRecordsRequestEntry> getRecordsRequestList(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<PutRecordsRequestEntry> putRecordsRequestEntries = new ArrayList<>();
        for (Order order: getOrderList()){
            PutRecordsRequestEntry requestEntry = new PutRecordsRequestEntry();
            requestEntry.setData(ByteBuffer.wrap(gson.toJson(order).getBytes()));
            requestEntry.setPartitionKey(UUID.randomUUID().toString());
            putRecordsRequestEntries.add(requestEntry);
        }
        return putRecordsRequestEntries;
    }

    private List<Order> getOrderList(){
        List<Order> orders = new ArrayList<>();
        for(int i=0;i<1;i++){
            Order order = new Order();
            order.setOrderId(Math.abs(random.nextInt()));
            order.setProduct(productList.get(random.nextInt(productList.size())));
            order.setQuantity(random.nextInt(20));
            order.setChangedBy("pradeep");
            order.setTestedBy("tester");
            orders.add(order);
            System.out.println(order.toString());
        }
        return orders;
    }

    private void populateProductList(){
        productList.add("shirt");
        productList.add("t-shirt");
        productList.add("shorts");
        productList.add("tie");
        productList.add("shoes");
        productList.add("jeans");
        productList.add("belt");
    }


}
