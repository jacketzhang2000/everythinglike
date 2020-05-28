package task;

import app.FileMeta;
import util.PinyintUtil;
import util.DBUtil;
import util.Util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileSave implements ScanCallback {

    public void callback(File dir) {
        File[] childen = dir.listFiles();
        List<FileMeta> locals = new ArrayList<>();
        if (childen != null) {
            for (File child : childen) {
                locals.add(new FileMeta(child));
            }
        }

        //获取数据库保存的dir目录下一级子文件和子文件夹
        //TODO List<file>
        List<FileMeta> metas=query(dir);
        //本地无，数据库有，删除
        for (FileMeta  meta:metas){
            if(!locals.contains(meta)){
                delete(meta);
            }
        }
        //TODO

        //本地有，数据库无，插入
        for(FileMeta meta:locals){
            if(!metas.contains(meta)){
                save(meta);
            }
        }
    }
    //meta的删除:1.删除信息本身，
    //2.如果是目录，删除其子文件
    public void delete(FileMeta meta) {
        Connection connection =null;
        PreparedStatement ps=null;
        try {
            connection=DBUtil.getConnection();
            String sql="delete from file_meta where "+
                    "(name=? and path=? and is_directory=?)";
            if(meta.getDirectory()) {
               sql+= " or path=?"+
               "or path like ?";
            }
            ps=connection.prepareStatement(sql);
            ps.setString(1,meta.getName());
            ps.setString(2,meta.getPath());
            ps.setBoolean(3,meta.getDirectory());
            if (meta.getDirectory()) {
                ps.setString(4,
                        meta.getPath() + File.separator + meta.getName());
                ps.setString(5,
                        meta.getPath() + File.separator + meta.getName() + File.separator);
                System.out.printf("delete dir=%s\n", meta.getPath() + File.separator + meta.getName());

                ps.executeUpdate();
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("删除文件信息出错,检查delete语句",e);
        }finally {
            DBUtil.close(connection,ps);
        }
    }

    private List<FileMeta> query(File dir) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<FileMeta> metas = new ArrayList<>();
        try {
            //1.创建数据库连接
            connection = DBUtil.getConnection();
            String sql;
            sql = "select name, path, is_directory,size,last_modified" +
                    " from file_meta where path=?";
            //2.创建Jdbc操作命令对象statment
            ps = connection.prepareStatement(sql);
            ps.setString(1, dir.getPath());
            //3.执行sql语句
            rs = ps.executeQuery();
            //4.处理结果集ResultSet

            while (rs.next()) {
                String name = rs.getString("name");
                String path = rs.getString("path");
                Boolean isDirectory = rs.getBoolean("is_directory");
                Long size = rs.getLong("size");
                Timestamp lastModified = rs.getTimestamp("last_modified");
                FileMeta meta = new FileMeta(name, path, isDirectory,
                        size, new java.util.Date(lastModified.getTime()));
                System.out.printf("查询文件信息：name=%s,path=%s,is_directory=%s," +
                                " size=%s,last_modified=%s\n", name, path, String.valueOf(isDirectory),
                        String.valueOf(size), Util.parseDate(new java.util.Date(lastModified.getTime())));

                metas.add(meta);
            }
            return metas;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询文件信息出错,检查sql语句", e);
        } finally {
            DBUtil.close(connection, ps, rs);
        }
    }

    /**
     * 文件信息保存到数据库
     *
     * @param file
     */
    private void save(FileMeta file) {

        Connection connection=null;
        PreparedStatement statement=null;

        try {

            //1.获取数据库连接
            connection = DBUtil.getConnection();
            String sql="insert into file_meta" +
                    "(name,path,is_directory,size,last_modified,pinyin,pinyin_first) " +
                    " VALUES (?,?,?,?,?,?,?)";

            //2.获取sql操作命令对象Statement
            statement=connection.prepareStatement(sql);
            System.out.println("执行文件保存操作："+sql);
            statement.setString(1, file.getName());
            statement.setString(2, file.getPath());
            statement.setBoolean(3,file.getDirectory());
            statement.setLong(4, file.getSize());
            statement.setString(5, file.getLastModifiedText());
            String pinyin=null;
            String pinyin_first=null;
            //文件包含汉字，需要获取拼音和首字母，并保存到数据库
            if (PinyintUtil.containsChinese(file.getName())) {
                String[] pinyins = PinyintUtil.get(file.getName());
                pinyin = pinyins[0];
                pinyin_first = pinyins[1];
            }
            statement.setString(6, file.getPinyin());
            statement.setString(7,file.getPinyinFirst());
            System.out.printf("insert name=%s,path=%s\n",file.getName(),file.getPath());
            //3.执行sql
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("文件保存失败，检查sql insert语句",e);
        } finally {
            DBUtil.close(connection,statement);
        }
    }

    public static void main(String[] args) {
//        DBInit.init();
//        File file = new File("F:\\大三上作业\\人工智能");
//        FileSave fileSave = new FileSave();
//        fileSave.save(file);
//        fileSave.query(file.getParentFile());
        List<FileMeta> locals=new ArrayList<>();
        locals.add(new FileMeta("新建文件夹","D:\\maventest副本",
                true,0l,new Date()));
        locals.add(new FileMeta("中华人民共和国","D:\\maventest副本",
                true,0l,new Date()));
        locals.add(new FileMeta("阿凡达.txt","D:\\maventest副本\\中华人民共和国",
                true,0l,new Date()));
        List<FileMeta> metas=new ArrayList<>();
        metas.add(new FileMeta("新建文件夹","D:\\maventest副本",
                true,0l,new Date()));
        metas.add(new FileMeta("中华人民共和国2","D:\\maventest副本",
                true,0l,new Date()));
        metas.add(new FileMeta("阿凡达.txt","D:\\maventest副本\\中华人民共和国2",
                true,0l,new Date()));
        //hashCode equals  ==
        for(FileMeta meta1:locals){
            if(!metas.contains(meta1)){
                System.out.println(meta1);
            }
        }

    }

}
