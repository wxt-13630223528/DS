package EverthingRoot.cmd;

import EverthingRoot.config.EverythingConfig;
import EverthingRoot.core.EverythingManager;
import EverthingRoot.core.model.Condition;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;



public class EverthingG2CmdApplicaton {
    public static void main(String[] args) {
        //0.Everything Config
        if(args.length>=1){
            String configFile = args[0];
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(configFile));
                //p的值 赋值给everythingConfig对象
                everythingConfigInit(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //1.EverthingManager
        EverythingManager manager = EverythingManager.getInstance();
        manager.Monitor();  // 开启文件修改的监控方法。

        //2.命令行交互
        Scanner scanner = new Scanner(System.in);

        //3.用户交互输入
        System.out.println("欢迎使用 Everthing G2");
        while(true) {
            System.out.println(">>");
            String line = scanner.nextLine();
            switch (line){
                case "help":
                    manager.help();
                    break;
                case "quit":
                    manager.quit();
                    break;
                case "index":
                    manager.buildIndex();  //之前检索会自己停下来，原因是数据库的Connection 每次用完都没有close
                    break;
                default :
                    if(line.startsWith("search")){
                        //解析参数
                        String[] segments = line.split(" ");
                        if(segments.length >= 2){
                            Condition condition = new Condition();
                            String name = segments[1];
                            condition.setName(name);
                            if(segments.length >= 3){
                                String type = segments[2];
                                condition.setFileType(type.toUpperCase());  //说明只能检测 文件的枚举大写字母
                            }
                            manager.search(condition).forEach(thing -> {
                                System.out.println(thing.getPath());
                            });
                        }else{
                            manager.help();
                        }
                    }else{
                        manager.help();
                    }
            }
        }
    }

    private static void everythingConfigInit(Properties p) {
        EverythingConfig config = EverythingConfig.geInstance();
        String max_return = p.getProperty("everything.max_return");
        System.out.println(max_return);
        String interval = p.getProperty("everything.interval");
        try{
            if(max_return != null){
                config.setMaxReturn(Integer.parseInt(max_return));
            }
            if(interval != null){
                config.setInterval(Integer.parseInt(interval));
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }



        String enableBuildIndex = p.getProperty("everything.enable_build_index");
        config.setEnableBuildIndex(Boolean.parseBoolean(enableBuildIndex));//传入null  返回false

        String orderByDesc = p.getProperty("everything.order_by_desc");
        config.setOrderByDepth(Boolean.parseBoolean(orderByDesc));

        //处理的目录
        String includePath = p.getProperty("everything.handle_path.include_path");

        if(includePath!=null){
            String[] paths = includePath.split(";");
            if(paths.length>0){
                //清理掉已经有的默认值
                config.getHandlePath().getIncludePath().clear();
                for(String path : paths){
                    config.getHandlePath().addIncludePath(path);
                }
            }
        }

        String excludePath = p.getProperty("everything.handle_path.exclude_path");
        if(includePath!=null){
            String[] paths = excludePath.split(";");
            if(paths.length>0){
                //清理掉已经有的默认值
                config.getHandlePath().getExcludePath().clear();
                for(String path : paths){
                    config.getHandlePath().addExcludePath(path);
                }
            }
        }
    }
}
