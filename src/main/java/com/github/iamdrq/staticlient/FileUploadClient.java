package com.github.iamdrq.staticlient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUploadClient {

    String url;
    String token;
    Map<String, String> map;

    public FileUploadClient(String url,String token){
        this.url=url;
        this.token=token;

        map=new HashMap<String, String>();
        map.put("token",token);
    }

    public FileUploadResult upload(String path,String name,InputStream inputStream){
        map.put("path",path);
        FileUploadResult fileUploadResult=new FileUploadResult();
        try{
            final String NEWLINE = "\r\n"; // 换行，或者说是回车
            final String PREFIX = "--"; // 固定的前缀
            final String BOUNDARY = "#"; // 分界线，就是上面提到的boundary，可以是任意字符串，建议写长一点，这里简单的写了一个#

            HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(url).openConnection();
            // 设置通用的请求属性
            httpURLConnection.setRequestProperty("accept","*/*");
            httpURLConnection.setRequestProperty("connection","Keep-Alive");
            httpURLConnection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            httpURLConnection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY);
            httpURLConnection.setConnectTimeout(3600*1000);
            httpURLConnection.setReadTimeout(3600*1000);
            // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            // 默认是 GET方式
            httpURLConnection.setRequestMethod("POST");
            // Post 请求不能使用缓存
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.connect();

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());

            if (map != null && !map.isEmpty()) { // 这时请求中的普通参数，键值对类型的，相当于上面分析的请求中的username，可能有多个
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey(); // 键，相当于上面分析的请求中的username
                    String value = map.get(key); // 值，相当于上面分析的请求中的sdafdsa
                    dos.writeBytes(PREFIX + BOUNDARY + NEWLINE); // 像请求体中写分割线，就是前缀+分界线+换行
                    dos.writeBytes("Content-Disposition: form-data; " + "name=\"" + key + "\"" + NEWLINE); // 拼接参数名，格式就是Content-Disposition: form-data; name="key" 其中key就是当前循环的键值对的键，别忘了最后的换行
                    dos.writeBytes(NEWLINE); // 空行，一定不能少，键和值之间有一个固定的空行
                    dos.writeBytes(URLEncoder.encode(value, "utf-8")); // 将值写入
                    //或者写成：dos.write(value.toString().getBytes(charset));
                    dos.writeBytes(NEWLINE); // 换行
                } // 所有循环完毕，就把所有的键值对都写入了
            }

            dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);// 像请求体中写分割线，就是前缀+分界线+换行
            // 格式是:Content-Disposition: form-data; name="请求参数名"; filename="文件名"
            // 我这里吧请求的参数名写成了uploadFile，是死的，实际应用要根据自己的情况修改
            // 不要忘了换行
            dos.writeBytes("Content-Disposition: form-data; " + "name=\"file\"" + "; filename=\"" + name+ "\"" + NEWLINE);
            // 换行，重要！！不要忘了
            dos.writeBytes(NEWLINE);
            byte[] buffer = new byte[1024];
            int len = -1;
            while((len = inputStream.read(buffer)) != -1){
                dos.write(buffer, 0, len);
            }
            dos.writeBytes(NEWLINE); // 最后换行

            dos.writeBytes(PREFIX + BOUNDARY + PREFIX + NEWLINE); // 最后的分割线，与前面的有点不一样是前缀+分界线+前缀+换行，最后多了一个前缀
            dos.flush();
            dos.close();

            InputStreamReader in =new InputStreamReader(httpURLConnection.getInputStream(),"utf-8");
            StringBuilder result=new StringBuilder();
            int line;
            while ((line=in.read()) != -1) {
                result.append((char)line);
            }
            in.close();
            System.out.println(result);

            ObjectMapper objectMapper = new ObjectMapper();
            Map map=objectMapper.readValue(result.toString(),Map.class);
            Integer code=(Integer)map.get("res");
            fileUploadResult.setCode(code);
            if(code==1){
                List<String> data=(List<String>)map.get("data");
                fileUploadResult.setPath(data.size()>0?data.get(0):"");
            }
            else{
                fileUploadResult.setMsg(map.get("data").toString());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            fileUploadResult.setCode(0);
            fileUploadResult.setMsg("error");
        }
        return fileUploadResult;
    }

    public static void main(String[] args) throws Exception{
        FileUploadClient fileUploadClient=new FileUploadClient("url","token");
        File file=new File("C:\\Users\\drq\\Desktop\\index.php");
        FileUploadResult fileUploadResult=fileUploadClient.upload("test/123",file.getName(),new FileInputStream(file));
        System.out.print(fileUploadResult);
    }

}