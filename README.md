# LambdaS3AdapterFromZipToFiles
A lambda that triggers off a zip file S3-PUT event. It takes the inserted zip file and decompresses it into a 
constant variable(SRC_BUCKET). 

This lambda function expects two folders at the root of the source S3 bucket: 
1. images
2. info

## images folder
This folder is where all the image files in the zip file are going to be extracted.

## info folder
This folder is where all the other files are stored.

