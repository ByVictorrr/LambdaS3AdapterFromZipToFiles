// File: App.java
// Lambda function that triggers off an s3 put by a zip file and decompresses it in another s3 bucket
package helloworld;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;


/**
 * This is the main App class that triggers off a s3 put of a zip file
 * and decompresses the files in another s3 bucket
 *
 * @see com.amazonaws.services.lambda.runtime
 * @see com.amazonaws.services.s3*
 *
 * @author byvictorrr
 */


public class App implements RequestHandler<S3EventNotification, Object> {


    final String SRC_BUCKET = "4n6-dynamodb-endpoint";
    final String ARN = "arn:aws:sns:us-west-2:515851393822:4N6";
    /**
     * Takes the contents from the <code>S3EventNotification</code>, then gets
     * the contents of the zip file specified in the S3 event. The contents are
     * then stored in a <code>ZipInputStream</code>. This is handed off to the
     * singleton class <code>ZipDecompressor</code> class.
     *
     * @param input a S3 trigger with information about it
     * @parm Context, im not really sure what this does
     * @return an <code> Object </code> that specifies result of the run
     */
    public Object handleRequest(final S3EventNotification input, final Context context) {
        final ZipDecompressor decompressor = ZipDecompressor.getInstance();
        S3EventNotification.S3Entity s3Entity;
        String key, eventBucket;
        ZipInputStream zin;

        AmazonSNS build = AmazonSNSClientBuilder.
                standard().
                withRegion(Regions.US_WEST_2).build();
        AmazonSNSClient snsClient = (AmazonSNSClient) build;

        try{

            s3Entity = input.getRecords().get(0).getS3();
            key = s3Entity.getObject().getKey();
            eventBucket = s3Entity.getBucket().getName();
            decompressor.decompress(eventBucket,SRC_BUCKET,key);


            // Finally lambda sends a SRC_BUCKET response to the next lambda to read the data
            final PublishRequest publishRequest = new PublishRequest(ARN, SRC_BUCKET);
            final PublishResult publishResult= snsClient.publish(publishRequest);

            System.out.println("Message id: " + publishResult.getMessageId());

        }catch (Exception e){
            e.printStackTrace();
        }


        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        return new GatewayResponse("{}", headers, 200);
    }


}
