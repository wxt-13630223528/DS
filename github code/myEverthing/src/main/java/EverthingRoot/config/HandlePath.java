package EverthingRoot.config;



import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 处理的目录
 */
public class HandlePath {
    //包含的目录
    private Set<String> includePath = new HashSet<>();
    //排除的目录
    private Set<String> excludePath = new HashSet<>();

    public Set<String> getIncludePath() {
        return includePath;
    }

    public Set<String> getExcludePath() {
        return excludePath;
    }

    private HandlePath(){   //直接用下面的静态方法

    }

    public void addIncludePath(String path){
        this.includePath.add(path);
    }

    public void addExcludePath(String path){
        this.excludePath.add(path);
    }

    public static HandlePath getDefaultHandlerPath(){
        //返回一个默认的路径
        HandlePath handlePath = new HandlePath();
        Iterable<Path> paths = FileSystems.getDefault().getRootDirectories();
        //默认要包含的目录，即构建索引是需要处理的路径
        paths.forEach(path -> {
            handlePath.addIncludePath(path.toString());
        });
        //默认要排除的目录，即构建索引时不需要处理的路径
        String systemName = System.getProperty("os.name");
        if(systemName.contains("Windows")){
            handlePath.addExcludePath("C:\\Windows");
            handlePath.addExcludePath("C:\\Program Files");
            handlePath.addExcludePath("C:\\Program Files(x86)");
            handlePath.addExcludePath("C:\\ProgramData");
        }else {
            handlePath.addExcludePath("/root");
            handlePath.addExcludePath("/temp");
        }
        return handlePath;
    }
}
