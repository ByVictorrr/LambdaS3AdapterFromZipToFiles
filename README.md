# LambdaS3AdapterFromZipToFiles
A lambda function that is used when a set S3-PUT request is made in a specific S3 bucket. The file that is inserted is assumed to be a zip file containing images and jsons files. This function takes the zip file and decompresses the zip file into the inputted S3 bucket name. 
