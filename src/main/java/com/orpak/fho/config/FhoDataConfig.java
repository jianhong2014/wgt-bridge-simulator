package com.orpak.fho.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


@Setter
@Getter
@Configuration
public class FhoDataConfig {

    private static final Logger logger = LoggerFactory.getLogger(FhoDataConfig.class);

    @Bean("fhDataMarshaller")
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // this package must match the package in the <generatePackage> specified in
        // pom.xml
        marshaller.setContextPath("com.gvr.datahub.sdk.orpak");
        return marshaller;
    }

    //@Bean
    public HttpComponentsMessageSender httpComponentsMessageSender() throws Exception {
        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
        logger.info("init HttpComponentsMessageSender1 ");
        HttpClient hc = httpClient();
        logger.info("init HttpComponentsMessageSender2 {} ",hc);
        httpComponentsMessageSender.setHttpClient(hc);
        logger.info("init HttpComponentsMessageSender3  ");
        return httpComponentsMessageSender;
    }

    //@Value("${fho.ws.default-uri}")
    //Ӧ�ó���Ҫ����IP first
    private String fhoIp = "10.28.188.35";

    private String wsUrl = "";

    //@Value("${fho.ws.trust-store}")
    private Resource trustStore;

    //@Value("${fho.ws.trust-store-password}")
    private String trustStorePassword = "123456";

    @Bean
    public WebServiceTemplate webServiceTemplate() throws Exception {
        wsUrl = "https://"+fhoIp+":2445/SiteOmatService/SiteOmatService.asmx";
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller());
        webServiceTemplate.setUnmarshaller(marshaller());
        webServiceTemplate.setDefaultUri(wsUrl);
        webServiceTemplate.setMessageSender(httpComponentsMessageSender());
        return webServiceTemplate;
    }

    public HttpClient httpClient() throws Exception {
        return HttpClientBuilder.create().setSSLSocketFactory(sslConnectionSocketFactory())
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor()).build();
    }

    public SSLConnectionSocketFactory sslConnectionSocketFactory() throws Exception {
        // NoopHostnameVerifier essentially turns hostname verification off as otherwise following error
        // is thrown: java.security.cert.CertificateException: No name matching localhost found
        return new SSLConnectionSocketFactory(sslContext(), NoopHostnameVerifier.INSTANCE);
    }

    public SSLContext sslContext() throws Exception {
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // ��������
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return sslContext;
        //return SSLContextBuilder.create()
          //      .loadTrustMaterial(trustStore.getFile(), trustStorePassword.toCharArray()).build();
    }

}
