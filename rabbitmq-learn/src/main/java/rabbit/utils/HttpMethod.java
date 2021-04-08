package rabbit.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import rabbit.ConfigConstants;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by fw on 2020/4/15
 */
public class HttpMethod {

    protected static HttpClient client = HttpClientBuilder.create().build();

    private static final String AUTH = ConfigConstants.USER_NAME + ":" + ConfigConstants.PASSWORD;
    private static final BasicHeader AUTHORIZATION_HEADER = new BasicHeader("Authorization", "Basic " + Base64.encodeBase64String(AUTH.getBytes()));

    private static String URI = "http://" + ConfigConstants.IP_ADDRESS + ":" +15672;

    private static String API = URI + "/api";

    @Test
    public void tesst(){
        String s = queueList(() -> "/exchanges");
        System.out.println("s = " + s);
    }

    public String  queueList(Supplier<String> function){

        // 参数
        // StringBuffer params = new StringBuffer();
        // try {
        //     // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
        //     params.append("user=" + URLEncoder.encode("&", "utf-8"));
        //     params.append("&");
        //     params.append("age=24");
        // } catch (UnsupportedEncodingException e1) {
        //     e1.printStackTrace();
        // }


        //设置用户名密码

        // 创建Get请求
        HttpGet httpGet = new HttpGet(API + function.get());
        httpGet.addHeader(AUTHORIZATION_HEADER);

        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Get请求
            response = (CloseableHttpResponse) client.execute(httpGet);

            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                return EntityUtils.toString(responseEntity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
