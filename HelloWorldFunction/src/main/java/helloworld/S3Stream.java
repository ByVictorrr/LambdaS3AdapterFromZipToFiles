package helloworld;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

public class S3Stream {
        private final String IMAGE_FOLDER = "images";
        private final String INFO_FOLDER = "info";

        private String bucketName;
        private AmazonS3Client client;

        public S3Stream(String bucketName){
            this.bucketName=bucketName;
            this.client=new AmazonS3Client();
        }
        public S3Stream(){
            this.client=new AmazonS3Client();
        }

        /**
         * @Param zipFileName is the name of the file that triggered the S3 PUT event
         * @Param bucket is the S3 bucket that triggered the non-helper function
         * @return a <code>ZipInputSteam</code> object that contains the contents
         *         of the zipFile that are first extracted from S3
         */
        public ZipInputStream getZipStream(String zipFileName) {
            S3Object object = client.getObject(bucketName, zipFileName);
            InputStream contents = object.getObjectContent();
            return new ZipInputStream(contents);
        }

    /**
     * Task is to write the contents of a InputStream to be stored as a
     * file in the bucket.
     *
     * @param fileName extracted filename
     * @param contents contents of the filename
     */
    public void writeFileToS3(String fileName, long length,
                              final InputStream contents,
                              final InputStream man)
    {
            String folder, manufacture;
            StringBuilder sb;
            try {
                if((folder = putFolder(fileName)).equals("info")) {
                        manufacture=getManufacture(man)+"/";
                        folder="";
                }else{
                    manufacture="";
                    folder+="/";
                }
                sb=new StringBuilder(manufacture)
                        .append(folder)
                        .append(fileName);

                ObjectMetadata md = new ObjectMetadata();
                md.setContentLength(length);
                this.client.putObject(
                        new PutObjectRequest(this.bucketName,
                                sb.toString(),
                                contents,
                                md


                        ).withCannedAcl(CannedAccessControlList.Private)
                );
                contents.close();
            }catch (Exception e){
                e.printStackTrace();
            }

    }
    private String getManufacture(final InputStream is)
            throws Exception
    {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = mapper.readValue(is, Map.class);
        return jsonMap.get("manufacturer").toString();
    }
    /**
     * Determines if a file name is an image of not
     * @param fileName name to be determined if its image like
     * @return Matcher.matches() which is true if image like name otherwise false
     */
    private boolean isImage(String fileName){
        final String IMAGE_PATTERN = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP|JPEG)$";
        Matcher matcher = Pattern.compile(IMAGE_PATTERN).matcher(fileName);
        return matcher.matches();
    }

    /**
     *
     * @param fileName used to determine the folder in which it should be writen to
     * @return IMAGE_FOLDER if fileName is image like and INFO_FOLDER if not
     */
    private String putFolder(String fileName){
            String folderName;
            if(isImage(fileName)){
                folderName=IMAGE_FOLDER;
            }else{
                folderName=INFO_FOLDER;
            }
            return folderName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
