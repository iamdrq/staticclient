# Staticlient

### Example：
FileUploadClient fileUploadClient=new FileUploadClient("url","token");
<br>
File file=new File("C:\\Users\\drq\\Desktop\\1.txt");
<br>
FileUploadResult fileUploadResult=fileUploadClient.upload("test",file.getName(),new FileInputStream(file));
