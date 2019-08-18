package EverthingRoot.core.search.impl;

import java.util.Queue;
import EverthingRoot.core.dao.FileIndexDao;
import EverthingRoot.core.interceptor.impl.ThingClearInterceptor;
import EverthingRoot.core.model.Condition;
import EverthingRoot.core.model.Thing;
import EverthingRoot.core.search.ThingSearch;

import java.util.Iterator;
import java.util.List;
import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ThingSearchImpl implements ThingSearch {

    private final FileIndexDao fileIndexDao;

    //创建拦截器对象
    private  ThingClearInterceptor interceptor;

    private final Queue<Thing> thingQueue = new ArrayBlockingQueue<>(1024); //清理队列

    public ThingSearchImpl(FileIndexDao fileIndexDao, ThingClearInterceptor interceptor) {
        this.fileIndexDao = fileIndexDao;

        this.interceptor = interceptor;

        this.backgroundClearThread();
    }

    public ThingSearchImpl(FileIndexDao fileIndexDao) {
        this.fileIndexDao = fileIndexDao;
    }

    //如果本地文件系统将文件删除，数据库中仍然存储到索引信息，此时如果查询结果存在已经在文件系统中删除的文件
    //那么需要在数据库中清除掉该文件的索引信息
    @Override
    public List<Thing> search(Condition condition) {
        List<Thing> things = this.fileIndexDao.query(condition);
        Iterator<Thing> iterator = things.iterator();
        while(iterator.hasNext()){
            Thing thing = iterator.next();
            File file = new File(thing.getPath());
            if(!file.exists()){
                //删除
                iterator.remove();   //CPU机密性   iterator.remove之后 things中也就remove了
                //IO阻塞   所以运用生产者消费者模型
                this.thingQueue.add(thing);   //确定这个文件不在电脑中之后，就应该将数据库中的这个文件信息清理掉
            }
        }
        return things;
    }

    private void backgroundClearThread(){
        //进行后台清理工作
        Thread thread = new Thread(this.interceptor);
        thread.setName("Thread-Clear");
        thread.setDaemon(true);  //守护线程
        thread.start();
    }

    //此处需要依赖数据库检索
}
