import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.io.File;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileSearchManage
{

    private List<File> files;

    private static Path directory;

    private static String ext;

    private static String text;

    private MutableTreeNode mtb;

    static {
        ext = "log";
        text = "Java";
    }

    public void scan()
    {
        files = new ArrayList<>();
        FutureTask task = new FutureTask(new ScanDir(directory.toFile(), ext));
        Thread thread = new Thread(task);
        thread.start();
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();
        try {
            treeNode =  (DefaultMutableTreeNode)task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        mtb = treeNode;
    }

    public MutableTreeNode getMtb() {
        return mtb;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setCurrentDirectory(Path path) {
        this.directory = path;
    }

    public Path getCurrentDirectory() {
        return this.directory;
    }

    public void setExtension(String ext) {
        this.ext = ext.trim();
    }

    public String getExtension() {
        return this.ext;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public boolean readyToSearch() {
        if (directory == null || text == null || text.equals("") || ext.equals("")
                || ext == null) {
            return false;
        } else {
            return true;
        }
    }

    class ScanDir implements Callable<MutableTreeNode> {

        private File node;
        private String ext;
        private ExecutorService executor = Executors.newFixedThreadPool(8);
        List<String> list = new ArrayList<>();

        public ScanDir(File node, String ext) {
            this.node = node;
            this.ext = ext;
        }

        @Override
        public MutableTreeNode call() throws ExecutionException, InterruptedException {

            list = new ArrayList<>();
            DefaultMutableTreeNode ret = new DefaultMutableTreeNode(node.getName());
            List<Future> results = new ArrayList<>();
               if (node.listFiles() != null) {
                   for (File child : node.listFiles()) {
                       if (child.isDirectory()) {
                               results.add(executor.submit(new ScanDir(child, ext)));
                       } else {
                           if (child.getName().endsWith("." + ext) && new DefiniteFile(child, text).manage()) {
                               files.add(child);
                               ret.add(new DefaultMutableTreeNode(child.getName()));
                           }
                       }
                   }
               }
            for(Future result : results) {
                DefaultMutableTreeNode defNode = (DefaultMutableTreeNode)result.get();
                if(defNode.getLastLeaf().toString().endsWith("." + ext)) {
                    ret.add(defNode);
                }
            }
            return ret;
        }
    }

}