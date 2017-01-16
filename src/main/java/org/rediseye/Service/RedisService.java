package org.rediseye.Service;

import org.rediseye.entity.ClusterState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Date: 2016/12/14 下午6:58
 * Usage:
 */
@Service
public class RedisService {
    @Autowired
    JedisConnectionFactory jedisConnectionFactory;

    public HashMap<String, Object> getInfo() {
        RedisClusterConnection conn = jedisConnectionFactory.getClusterConnection();
        Iterable<RedisClusterNode> nodes = conn.clusterGetNodes();
        long keysCount = conn.dbSize();
        HashMap<String, Object> infos = new HashMap<>();
        ClusterInfo clusterInfo = conn.clusterGetClusterInfo();
        ClusterState clusterState = new ClusterState(clusterInfo);
        clusterState.setKeysCount(keysCount);
        ArrayList<Properties> nodesInfo = new ArrayList<>();
        nodes.forEach(node -> {
            Properties info;
            if (node.isConnected()) {
                info = conn.info(node, "memory");
            } else {
                info = new Properties();
            }
            info.put("server", node.getHost() + ":" + node.getPort());
            info.put("role", node.getType().name());
            info.put("state", node.getLinkState().name());
            info.put("id", node.getId());
            nodesInfo.add(info);
        });
        infos.put("clusterInfo", clusterState);
        infos.put("nodesInfo", nodesInfo);
        infos.put("time", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return infos;
    }

    public Long deleteKey(String key) {
        RedisClusterConnection conn = jedisConnectionFactory.getClusterConnection();
        return conn.del(key.getBytes());
    }

    public HashMap<String, Properties> getNodeInfos() {
        RedisClusterConnection conn = jedisConnectionFactory.getClusterConnection();
        Iterable<RedisClusterNode> nodes = conn.clusterGetNodes();
        RedisClusterNode node = null;
        Properties info;
        HashMap<String, Properties> infos = new HashMap<>();
        for (RedisClusterNode n : nodes) {
            info = conn.info(n, "memory");
            infos.put(n.getId(), info);
        }
        return infos;
    }

}
