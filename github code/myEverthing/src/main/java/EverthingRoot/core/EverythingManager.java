package EverthingRoot.core;


import EverthingRoot.config.EverythingConfig;
import EverthingRoot.config.HandlePath;
import EverthingRoot.core.dao.DataSourceFactory;
import EverthingRoot.core.dao.FileIndexDao;
import EverthingRoot.core.dao.impl.FileIndexDaoImpl;
import EverthingRoot.core.index.FileScan;
import EverthingRoot.core.index.imlp.FileScanImpl;
import EverthingRoot.core.interceptor.impl.FileIndexInterceptor;
import EverthingRoot.core.model.Condition;
import EverthingRoot.core.model.Thing;
import EverthingRoot.core.monitor.FileMonitor;
import EverthingRoot.core.monitor.Impl.FileMonitorImpl;
import EverthingRoot.core.search.ThingSearch;
import EverthingRoot.core.search.impl.ThingSearchImpl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 核心的统一调度器
 * 1.索引
 * 2.检索
 * 配置，索引模块，检索模块，拦截器模块  组合在一起，进行调度
 */
public class EverythingManager {
    //希望调度器是单例
    private static volatile  EverythingManager manager;

    /**
     * 业务层
     */
    private FileScan fileScan;  //构建索引

    private ThingSearch thingSearch;


    private EverythingConfig config = EverythingConfig.geInstance();
    /**
     * 线程池的执行器
     */

    private FileMonitor fileMonitor;

    private final ExecutorService excutorService = Executors.newFixedThreadPool
            (Runtime.getRuntime().availableProcessors());


    //因为单例模式 所以构造方法是私有的
    private EverythingManager(){

        /*
      数据库访问层
     */ /**
         * 数据库访问层
         */
     FileIndexDao fileIndexDao = new FileIndexDaoImpl(DataSourceFactory.getInstance());
        this.fileScan = new FileScanImpl();
         //打印索引信息的拦截器
        /**
         * this.fileScan.interceptor(new FilePrintInterceptor());
         */
        //索引信息写入数据库的拦截器
        this.fileScan.interceptor(new FileIndexInterceptor(fileIndexDao));

        this.thingSearch = new ThingSearchImpl(fileIndexDao);

        this.fileMonitor = new FileMonitorImpl(fileIndexDao);
    }

    public static EverythingManager getInstance(){
        //两次检查方式
        if(manager == null){
            synchronized (EverythingManager.class){
                if(manager == null){
                    manager = new EverythingManager();
                }
            }
        }
        return manager;
    }

    /**
     * 构建索引
     */
    public void buildIndex(){
        //建立索引
        DataSourceFactory.databaseInit(true);  //将旧表删除，创建一个新表。

        HandlePath handlePath = config.getHandlePath();

        Set<String> includePaths = handlePath.getIncludePath();
        new Thread(() -> {
            System.out.println("Build Index Started...");
            //countDownLatch定义一个类似线程倒计时的东西，参数就是时间
            final CountDownLatch countDownLatch = new CountDownLatch(includePaths.size());

            for(String path : includePaths){
                excutorService.submit(() -> {
                    fileScan.index(path);
                    //调用这个方法的时候，倒计时的时间 减一。
                   countDownLatch.countDown();
                });
            }

           try {
                //在这里检测 是否 倒计时已经为0了，  要不然不会放行。
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Build Index complete...");
        }).start();


    }

    /**
     * 检索功能
     */

    public List<Thing> search(Condition condition){
        //Condition 用户提供的是 ： name filetype
        //limit orderBy
        condition.setLimit(config.getMaxReturn());
        condition.setOrderByDepthAsc(!config.getOrderByDepth());
        return this.thingSearch.search(condition);
    }

    /**
     * 帮助功能
     */
    public void help(){
        System.out.println("命令列表：");
        System.out.println("退出：quit");
        System.out.println("帮助：help");
        System.out.println("索引：index");
        System.out.println("搜索：search<name> [<file-Type> img | doc | bin | archive | other]");
    }

    /**
     * 退出
     */
    public void quit(){
        System.out.println("欢迎使用，再见！");
        System.exit(0);
    }


    //启动文件监控
    public void Monitor(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                fileMonitor.monitor(config.getHandlePath());
                fileMonitor.start();
            }
        }).start();
    }
}
