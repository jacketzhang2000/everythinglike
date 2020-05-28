package app;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import jdk.internal.dynalink.linker.LinkerServices;
import task.*;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private GridPane rootPane;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<FileMeta> fileTable;

    @FXML
    private Label srcDirectory;

    private Thread task;

    public void initialize(URL location, ResourceBundle resources) {
        //界面初始化，需要初始化数据库和数据库表
        DBInit.init();
        // 添加搜索框监听器，内容改变时执行监听事件

        searchField.textProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                freshTable();
            }
        });
    }

    public void choose(Event event) {
        // 选择文件目录
        DirectoryChooser directoryChooser=new DirectoryChooser();
        Window window = rootPane.getScene().getWindow();
        File file = directoryChooser.showDialog(window);
        if(file == null)
            return;
        // 获取选择的目录路径，并显示
        String path = file.getPath();

        srcDirectory.setText(path);
        if(task!=null){
            task.interrupt();
        }
        task=new Thread(new Runnable() {
            @Override
            public void run() {
                ScanCallback callback=new FileSave();
                FileScanner scanner = new FileScanner(callback);
                try {
                    System.out.println("执行文件扫描任务");
                    scanner.scan(path);
                    System.out.println("等待扫描结束"+path);
                    scanner.waitFinsh();
                    System.out.println("任务执行完毕，刷新表格");
                    //为提高效率，多线程执行扫描任务
                    //等待文件扫描认为执行完毕
                    //TODO
                    //刷新表格
                    freshTable();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        task.start();
    }

    // 刷新表格数据
    private void freshTable(){
        ObservableList<FileMeta> metas = fileTable.getItems();
        metas.clear();
        // 如果选择了某个目录，代表需要再根据搜索框的内容，进行数据库文件信息的查询
        String dir=srcDirectory.getText();
        if(dir!=null&&dir.trim().length()!=0){
            String content=searchField.getText();
            //提供数据库的查询方法
            List<FileMeta> fileMetas= FileSearch.search(dir,content);

            //TODO
            //Collection
            metas.addAll(fileMetas);

        }
        //方法返回后，javafx表单做什么
        //通过反射获取FileMeta类型中的属性（qpp.fxml文件中定义的属性）

    }
}