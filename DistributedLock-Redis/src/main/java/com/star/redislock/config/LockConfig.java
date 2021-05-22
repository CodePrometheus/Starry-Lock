package com.star.redislock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: zzStar
 * @Date: 05-18-2021 13:10
 */
@Data
@ConfigurationProperties(prefix = LockConfig.PREFIX)
public class LockConfig {

    private long waitTime = 60;

    private long leaseTime = 60;

    public static final String PREFIX = "starry.lock";

    private String url;

    private String password;

    private int database = 1;

    private ClusterServer clusterServer;

    private String codec = "org.redisson.codec.JsonJacksonCodec";


    public static class ClusterServer{

        private String[] nodeAddresses;

        public String[] getNodeAddresses() {
            return nodeAddresses;
        }

        public void setNodeAddresses(String[] nodeAddresses) {
            this.nodeAddresses = nodeAddresses;
        }
    }
}
