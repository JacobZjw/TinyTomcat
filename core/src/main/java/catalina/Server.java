package catalina;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.system.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import util.XmlUtil;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author JwZheng
 * @date 2021/7/26 14:48
 */
@Slf4j
public class Server {
    private Map<String, Service> services;

    public Server(File serverXML) throws Exception {
        this.services = XmlUtil.parseService(this, serverXML);
    }

    private void init() throws Exception {
        if (CollectionUtil.isEmpty(services)) {
            throw new Exception("services为空");
        }
        for (Service service : services.values()) {
            service.start();
        }
    }

    public void start() throws Exception {
        TimeInterval timer = DateUtil.timer();
        loadSystemInfo();
        init();
        log.info("Init has finished in {} ms", timer.intervalMs());
    }

    public void shutdown() throws Exception {
        if (CollectionUtil.isEmpty(services)) {
            throw new Exception("services为空");
        }
        for (Service service : services.values()) {
            service.shutdown();
        }
    }

    private void loadSystemInfo() {
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server Name", "TinyTomcat");
        infos.put("Server Version", "0.9.9");
        infos.put("Server Built", DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss"));

        infos.put("OS Name", SystemUtil.getOsInfo().getName());
        infos.put("OS Version", SystemUtil.getOsInfo().getVersion());
        infos.put("OS Arch", SystemUtil.getOsInfo().getArch());

        infos.put("Java Runtime Name",SystemUtil.getJavaRuntimeInfo().getName());
        infos.put("Java Runtime Version", SystemUtil.getJavaRuntimeInfo().getVersion());
        infos.put("Java Home Dir", SystemUtil.getJavaRuntimeInfo().getHomeDir());

        log.info("------------------------------------------");
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            log.info(String.format("%-22s\t%s",key,value));
        }
        log.info("------------------------------------------");
    }

    @Override
    public String toString() {
        return "Server{" +
                "services=" + services +
                '}';
    }
}
