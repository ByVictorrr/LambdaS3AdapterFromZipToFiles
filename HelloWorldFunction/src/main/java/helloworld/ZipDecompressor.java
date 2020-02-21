
package helloworld;

import com.amazonaws.util.IOUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipDecompressor {
    static private ZipDecompressor instance;
    static private S3Stream s3Stream;

    private ZipDecompressor(){}

    public static ZipDecompressor getInstance() {
        if(instance == null && s3Stream ==null) {
            instance = new ZipDecompressor();
            s3Stream = new S3Stream();
        }
        return instance;
    }

    /**
     * <code>ZipDecompressor</code> main function and job is to get each files info from the zipFile
     * coming from the srcBucket. Then hands of the job to <code>S3Stream</code> object to write the stream
     * to the destBucket
     * @param srcBucket contains the zip file to be decompressed
     * @param destBucket where the zip file is to be decompressed
     * @throws IOException
     */
    public void decompress(String srcBucket, String destBucket, String zipName) throws IOException
    {
        ZipInputStream zipStream;
        ZipEntry ze;
        try{
            s3Stream.setBucketName(srcBucket);
            zipStream = s3Stream.getZipStream(zipName);
            s3Stream.setBucketName(destBucket);
            while ((ze = zipStream.getNextEntry()) != null) {
                String zeName = FilenameUtils.getName(ze.getName());
                if (!ze.isDirectory()) {
                    s3Stream.writeFileToS3(zeName,convertZipInputStreamToInputStream(zipStream));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Helper function for decompress look above
     * @param \in is the <code>ZipInputStream</code> object that stores data for each entry
     * @return converted object
     * @throws IOException
     */
    private InputStream convertZipInputStreamToInputStream(final ZipInputStream in)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        InputStream is = new ByteArrayInputStream(out.toByteArray());
        return is;
    }


}
