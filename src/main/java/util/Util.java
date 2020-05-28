package util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static final String DATE_PATTERN="yyyy-MM-dd HH:mm:ss" ;

    public static String parseSize(long size) {
        String[] danweis={"B","KB","MB","GB","PB","TB"};
        int idx=0;
        while(size>1024&&idx<danweis.length-1){
            size/=1024;
            idx++;
        }
        return size+danweis[idx];
    }

    /**
     * 解析日期为中文日期描述
     * @param lastModified
     * @return
     */
    public static String parseDate(Date lastModified) {
        return new SimpleDateFormat(DATE_PATTERN).format(lastModified);
    }

    public static void main(String[] args) {
        System.out.println(new File("c:\\Users\\HP\\Desktop\\pdf\\3.JavaWeb").length());
        System.out.println(parseSize(10000000000000l));
        System.out.println(parseDate(new Date()));
    }
}
