package com.star.redislock.config;

import com.star.redislock.aspect.BusinessKey;
import com.star.redislock.aspect.LockInfoProvider;
import com.star.redislock.aspect.SLockAspect;
import com.star.redislock.lock.LockFactory;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

import javax.annotation.Resource;

/**
 * 装配Bean
 *
 * @Author: zzStar
 * @Date: 05-22-2021 12:38
 */
@Configuration
@ConditionalOnProperty(prefix = LockConfig.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(LockConfig.class)
@Import(SLockAspect.class)
class SLockAutoConfig {

    @Resource
    private LockConfig lockConfig;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public RedissonClient redissonClient() throws Exception {
        Config config = new Config();
        if (lockConfig.getClusterServer() != null) {
            config.useClusterServers().setPassword(lockConfig.getPassword())
                    .addNodeAddress(lockConfig.getClusterServer().getNodeAddresses());
        } else {
            config.useSingleServer().setAddress(lockConfig.getUrl())
                    .setDatabase(lockConfig.getDatabase())
                    .setPassword(lockConfig.getPassword());
        }

        Codec codec = (Codec) ClassUtils.forName(lockConfig.getCodec(), ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public LockInfoProvider lockInfoProvider() {
        return new LockInfoProvider();
    }

    @Bean
    public BusinessKey businessKey() {
        return new BusinessKey();
    }

    @Bean
    public LockFactory lockFactory() {
        return new LockFactory();
    }
}
