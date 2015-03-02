package demo;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.PreDestroy;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class SpringConfig extends WebMvcConfigurerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SpringConfig.class);

    public static final String PHANTOMJS = "/usr/bin/phantomjs";

    @Bean(destroyMethod = "quit")
    public PhantomJSDriver webDriver() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOMJS);
        PhantomJSDriver driver = new PhantomJSDriver(caps);
        driver.setLogLevel(Level.WARNING);
        driver.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
        driver.manage().window().setSize(new Dimension(1920, 1200));
        return driver;
    }

    /**
     * This is just some demo/debugging code to illustrate a problem with UrlChecker creating new threads during tomcat
     * shutdown.
     */
    @PreDestroy
    public void destroy() {
        System.out.println("Predestroy...");
        boolean hasUrlChecker = true;
        while (hasUrlChecker) {
            int urlCheckerCount = 0;
            Thread[] allThreads = getAllThreads();
            for (Thread t : allThreads) {
                System.out.println(t.getName());
                if (t.getName().startsWith("UrlChecker")) {
                    urlCheckerCount++;
                    LOG.info("Explicitely stopping UrlChecker thread: " + t.getName());
                    t.stop();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        // Do nothing here...
                        LOG.error("Error while waiting for thread " + t.getName() + " to stop.");
                    }
                }
            }
            hasUrlChecker = urlCheckerCount > 0;
        }

    }

    ThreadGroup getRootThreadGroup() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        ThreadGroup ptg;
        while ((ptg = tg.getParent()) != null)
            tg = ptg;
        return tg;
    }

    Thread[] getAllThreads() {
        final ThreadGroup root = getRootThreadGroup();
        final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        int nAlloc = thbean.getThreadCount();
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        return java.util.Arrays.copyOf(threads, n);
    }
}
