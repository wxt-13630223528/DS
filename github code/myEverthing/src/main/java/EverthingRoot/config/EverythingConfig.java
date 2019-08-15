package EverthingRoot.config;


import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.InputStream;

/**
 * 这是Everything的配置信息
 */
public class EverythingConfig {
    private static volatile EverythingConfig config;

    //索引目录
    private HandlePath handlePath = HandlePath.getDefaultHandlerPath();    //得到默认的要检索的路径和排除的路径

    public HandlePath getHandlePath() {
        return handlePath;
    }



    //最大检索返回的结果数
    private Integer maxReturn = 30;

    public Integer getMaxReturn() {
        return maxReturn;
    }

    public void setMaxReturn(Integer maxReturn) {
        this.maxReturn = maxReturn;
    }


    /**
     * 是否开启构建索引
     * 默认：程序运行即关闭构建索引
     * 开启实时机：
     * 1.运行程序时，指定参数
     * 2.通过功能命令index
     */
    private Boolean enableBuildIndex = false;

    public Boolean getEnableBuildIndex() {
        return enableBuildIndex;
    }

    public void setEnableBuildIndex(Boolean enableBuildIndex) {
        this.enableBuildIndex = enableBuildIndex;
    }

    /**
     * 检索时depth深度的排序规则
     * true:表示降序
     * false:表示升序
     * 默认是升序
     */
    private Boolean orderByDepth = false;

    public Boolean getOrderByDepth() {
        return orderByDepth;
    }

    public void setOrderByDepth(Boolean orderByDepth) {
        this.orderByDepth = orderByDepth;
    }


    /**
     * 间隔多久进行一次文件监控
     */
    private Integer interval = 60000;

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return "EverythingConfig{" +
                "handlePath=" + handlePath +
                ", maxReturn=" + maxReturn +
                ", enableBuildIndex=" + enableBuildIndex +
                ", orderByDepth=" + orderByDepth +
                '}';
    }

    private EverythingConfig(){
    }



    //单例模式
    public static EverythingConfig geInstance(){
        if(config == null){
            synchronized (EverythingConfig.class){
                if(config == null){
                    config = new EverythingConfig();
                }
            }
        }
        return config;
    }

}
